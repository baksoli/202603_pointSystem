package com.payments.pointsystem;

import com.payments.pointsystem.domain.Point;
import com.payments.pointsystem.domain.User;
import com.payments.pointsystem.repository.PointRepository;
import com.payments.pointsystem.repository.UserRepository;
import com.payments.pointsystem.service.PointService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional // 테스트 후 데이터를 자동 롤백해줍니다.
public class PointServiceExampleTest {
    @Autowired private PointService pointService;
    @Autowired private UserRepository userRepository;
    @Autowired private PointRepository pointRepository;

    @Test
    @DisplayName("사용 취소 시 원천 포인트가 만료되었다면 신규 포인트를 적립하고, 미만료 시 잔액을 복구한다")
    void complex_cancel_use_with_expiration() {
        // 1. Given: 기초 데이터 세팅
        User user = userRepository.save(User.builder().totalPoint(0L).build());
        Long userSeq = user.getUserId(); // 솔이 님의 엔티티 필드명에 맞게 확인하세요!

        // A: 1000원 적립, B: 500원 적립
        Long idA = pointService.addPoint(userSeq, 1000L, Point.PointType.NORMAL);
        Long idB = pointService.addPoint(userSeq, 500L, Point.PointType.NORMAL);

        // 2. 1200원 사용 (A: 1000차감, B: 200차감 -> 남은 잔액 300)
        Long idC = pointService.usePoint(userSeq, 1200L, "A1234");

        // 3. [강제 조작] A 포인트 만료 처리
        Point pointA = pointRepository.findById(idA).get();
        pointA.setExpiredAt(LocalDateTime.now().minusDays(1));
        pointRepository.saveAndFlush(pointA); // 즉시 반영

        // 4. When: 1100원 부분 사용 취소 실행
        pointService.cancelUse(idC, 1100L);

        // 5. Then: 결과 검증
        User updatedUser = userRepository.findById(userSeq).get();

        // 최종 잔액: 300 + 1100 = 1400L
        assertThat(updatedUser.getTotalPoint()).isEqualTo(1400L);

        // B 검증: 만료 안 됐으므로 300 -> 400원으로 복구됨
        Point pointB = pointRepository.findById(idB).get();
        assertThat(pointB.getRemainAmount()).isEqualTo(400L);

        // E 검증: A 대신 새로 적립된 1000원(REISSUE) 확인
        List<Point> newPoints = pointRepository.findAllByUserId(updatedUser);
        boolean hasNewPointE = newPoints.stream()
                .anyMatch(p -> p.getRemainAmount() == 1000L
                        && p.getExpiredAt().isAfter(LocalDateTime.now()));
        assertThat(hasNewPointE).isTrue();
    }
}
