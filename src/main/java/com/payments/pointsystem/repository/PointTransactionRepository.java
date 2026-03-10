package com.payments.pointsystem.repository;

import com.payments.pointsystem.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByOrderNo(String orderNo);
}