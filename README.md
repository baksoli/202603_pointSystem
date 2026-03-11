# 💰 무료 포인트 시스템 (Free Point System API)

고가용성과 데이터 정합성을 고려하여 설계된 포인트 적립, 사용, 취소 RESTful API 서비스입니다.
동적 정책 관리와 1원 단위의 정밀한 이력 추적을 지원하며, 다중 트랜잭션 환경에서의 안전한 포인트 소진 로직을 포함합니다.

## 🛠 Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.3.x
- **Database:** H2 (In-memory, 과제 제출용) / Spring Data JPA
- **Build Tool:** Gradle

## 🏗️ 시스템 아키텍처 및 설계 주안점

본 프로젝트는 단일 서버(H2) 환경에서 구동되도록 구현했습니다.

Database Console (H2)

애플리케이션 실행 후 접속 주소: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb

Username: sa / Password:(없음)

- **비관적 락(Pessimistic Lock) 적용:** 동일 사용자에 대한 포인트 적립/사용 요청이 동시에 발생할 경우 데이터 정합성이 깨질 수 있는 Race Condition을 방지하기 위해,
JPA의 LockModeType.PESSIMISTIC_WRITE를 사용하여 데이터베이스 수준에서 트랜잭션 직렬화를 보장했습니다.

## 📌 핵심 비즈니스 로직 및 요구사항 구현

### 1. 포인트 적립 (Earn)

- **동적 한도 제어:** 1회 최소/최대 적립 가능 포인트(기본 1~100,000) 및 개인별 최대 보유 한도를 하드코딩하지 않고, DB의 `point_policies` 테이블을 통해 런타임에 제어합니다.
- **수기 지급 식별:** 관리자가 수기로 지급한 포인트는 `point_type`을 `ADMIN_MANUAL`로 구분하여 별도 관리합니다.
- **유효기간 부여:** 적립 시점부터 최소 1일 ~ 최대 5년 미만의 유효기간을 동적으로 할당합니다.

### 2. 포인트 적립 취소 (Earn Cancel)

- 특정 적립 트랜잭션의 원본 금액만큼 취소합니다.
- **방어 로직:** 해당 적립 건의 잔액(`remain_amount`)이 초기 적립 금액(`initial_amount`)과 다를 경우(이미 일부가 사용된 경우) 취소를 거절(Fail-fast) 처리합니다.

### 3. 포인트 사용 (Use)

- 주문 번호(`order_no`)를 필수로 매핑하여 사용처를 식별합니다.
- **사용 상세 추적 (`point_usage_details`):** 하나의 주문에서 여러 개의 원천 포인트가 복합적으로 사용될 때, 어떤 포인트에서 얼마(1원 단위)가 차감되었는지 정밀하게 기록합니다.
- **우선순위 소진 알고리즘:**
    1. 관리자 수기 지급 포인트 (`ADMIN_MANUAL`) 우선 소진
    2. 만료일이 가장 짧게 남은 포인트 우선 소진 (`ORDER BY expired_at ASC`)

### 4. 포인트 사용 취소 (Use Cancel)

- 부분 취소 및 전체 취소를 모두 지원합니다.
- **재적립(Re-issue) 정책:** 사용 취소로 인해 복구되는 원천 포인트가 이미 만료일을 지난 경우, 해당 금액만큼 새로운 만료일을 부여하여 신규 포인트로 재적립 처리합니다.

## 🔌 API 명세 (Endpoints)

- `POST /api/v1/points/earn` : 포인트 적립
- `POST /api/v1/points/earns/{transactionId}/cancel` : 적립 취소
- `POST /api/v1/points/use` : 포인트 사용
- `POST /api/v1/points/uses/{transactionId}/cancel` : 사용 취소

## 🔌 프로젝트 구조(Package Structure)
src/main/java/com/payments/pointsystem/
├── controller/     # REST Controller 
├── domain/         # Entity 및 공통 도메인 로직 (Point, User, etc.)
├── repository/     # Spring Data JPA 인터페이스
└── service/        # 핵심 비즈니스 로직 (PointService)

## 🚀 Build & Run

1. **Repository Clone**
    
    ```bash
    git clone [저장소 URL]
    ```
    
2. build

./gradlew clean build

1. run

java -jar build/libs/point-system-0.0.1-SNAPSHOT.jar
