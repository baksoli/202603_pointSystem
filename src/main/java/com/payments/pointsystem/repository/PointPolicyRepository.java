package com.payments.pointsystem.repository;

import com.payments.pointsystem.domain.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
    Optional<PointPolicy> findByPolicyName(String policyName);
}