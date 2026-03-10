package com.payments.pointsystem.repository;

import com.payments.pointsystem.domain.PointTransaction;
import com.payments.pointsystem.domain.PointUsageDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, Long> {
    // 상세 내역 조회
    List<PointUsageDetail> findByTransactionId(PointTransaction transaction);
}