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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PointServiceTest {

    @Autowired private PointService pointService;
    @Autowired private UserRepository userRepository;
    @Autowired private PointRepository pointRepository;

    @Test
    @DisplayName("포인트 사용 시 유효기간이 짧은 것부터 순서대로 차감되어야 한다")
    void usePoint_ShouldDeductInOrder() {
        // 1. Given: 유저 생성 및 유효기간이 다른 포인트 2개 적립
        User user = User.builder().totalPoint(0L).build();
        userRepository.save(user);

        // A: 10일 뒤 만료되는 1000원
        pointService.addPoint(user.getUserId(), 1000L, Point.PointType.NORMAL);
        // B: 5일 뒤 만료되는 500원

        // 2. When: 700원 사용
        pointService.usePoint(user.getUserId(), 700L, "ORDER-001");

        // 3. Then: 유저 잔액 확인
        User updatedUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(updatedUser.getTotalPoint()).isEqualTo(300L);
    }

    @Test
    @DisplayName("잔액보다 큰 금액을 사용하려 하면 예외가 발생한다")
    void usePoint_ShouldThrowException_WhenInsufficient() {
        // Given
        User user = User.builder().totalPoint(500L).build();
        userRepository.save(user);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.usePoint(user.getUserId(), 1000L, "ORDER-002");
        });
    }

    @Test
    @DisplayName("여러 개의 포인트 조각이 있을 때 순서대로 차감된다")
    void usePoint_MultiplePoints() {
        // Given: 100원, 200원, 300원 순차 적립
        User user = User.builder().totalPoint(0L).build();
        userRepository.save(user);

        pointService.addPoint(user.getUserId(), 100L, Point.PointType.NORMAL);
        pointService.addPoint(user.getUserId(), 200L, Point.PointType.NORMAL);
        pointService.addPoint(user.getUserId(), 300L, Point.PointType.NORMAL);

        // When: 400원 사용
        pointService.usePoint(user.getUserId(), 400L, "ORDER-003");

        // Then: 총 600 - 400 = 200원 남아야 함
        User updatedUser = userRepository.findById(user.getUserId()).get();
        assertThat(updatedUser.getTotalPoint()).isEqualTo(200L);
    }
}