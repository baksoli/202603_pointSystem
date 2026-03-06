# 202603_pointSystem
# 무료 포인트 시스템 (Free Point System API)

1. 아래 기능을 만족하는 무료포인트 시스템(API)를 개발해야 하며 github에 올려주시면 됩니다. (필수)
2. ERD 를 PDF 또는 이미지 파일등의 형식으로 resource 하위에 포함해 주세요. (필수)
3. AWS 기반으로 서비스한다고 가정했을때의 아키텍처 구성을 PDF 또는 이미지 파일형식으로 resource 하위에 포함해 주세요 (옵션)
4. 빌드 방법과 과제 설명등은 README.md 파일에 작성해 주세요. (필수)

## 🛠 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.3.x
- **Database:** H2 (In-memory, 과제 제출용) / Spring Data JPA
- **Build Tool:** Gradle

## 🏗️ 시스템 아키텍처 및 설계 고려사항
본 프로젝트는 단일 서버(H2) 환경에서 구동되도록 구현되었으나, AWS 환경에서의 운영을 가정하고 설계했습니다.
- **동시성 제어:** 다중 서버 환경에서 동일 유저의 포인트 동시 접근(Race Condition)을 방어하기 위해 분산 락(Distributed Lock) 구조를 고려하여 설계했습니다.
- **확장성 보장:** 무결성 보장을 위해 메인 RDBMS(Aurora 타겟)를 사용하며, 서버 스케일 아웃(ECS Fargate 타겟)에 유연하게 대응할 수 있는 Stateless API로 구성했습니다.

## 📌 핵심 비즈니스 로직 및 요구사항 구현

### 1. 포인트 적립 (Add)
- **동적 한도 제어:** 1회 최소/최대 적립 가능 포인트(기본 1~100,000) 및 개인별 최대 보유 한도를 하드코딩하지 않고, DB의 `point_policies` 테이블을 통해 런타임에 제어합니다.
- **수기 지급 식별:** 관리자가 수기로 지급한 포인트는 `point_type`을 `ADMIN_MANUAL`로 구분하여 별도 관리합니다.
- **유효기간 부여:** 적립 시점부터 최소 1일 ~ 최대 5년 미만의 유효기간을 동적으로 할당합니다.

### 2. 포인트 적립 취소 (Add Cancel)
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
- 포인트 적립 : `POST /api/v1/points/add`
- 적립 취소 : `POST /api/v1/points/add/{transactionId}/cancel`
- 포인트 사용 : `POST /api/v1/points/use`
- 포인트 사용 취소 : `POST /api/v1/points/uses/{transactionId}/cancel`

## 🚀 Build & Run
