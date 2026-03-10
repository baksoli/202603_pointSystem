package com.payments.pointsystem.repository;

import com.payments.pointsystem.domain.Point;
import com.payments.pointsystem.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    // 수기 지급(ADMIN_MANUAL) 0순위로, 그 다음 만료일 오름차순으로 정렬
    @Query("SELECT p FROM Point p " +
            "WHERE p.userId = :user " +
            "AND p.remainAmount > 0 " +
            "AND p.expiredAt > :now " +
            "ORDER BY CASE WHEN p.type = 'ADMIN_MANUAL' THEN 0 ELSE 1 END ASC, p.expiredAt ASC")
    List<Point> findAvailablePoints(@Param("user") User user, @Param("now") LocalDateTime now);

    // 적립 트랜잭션 ID로 원천 포인트 조회
    Optional<Point> findByEarnTransactionId(Long earnTransactionId);

    // 특정 유저의 모든 포인트 내역 조회
    List<Point> findAllByUserId(User user);
   }