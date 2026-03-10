package com.payments.pointsystem.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_name", unique = true, nullable = false)
    private String policyName;

    @Column(name = "min_value")
    private Long minValue;

    @Column(name = "max_value")
    private Long maxValue;
}