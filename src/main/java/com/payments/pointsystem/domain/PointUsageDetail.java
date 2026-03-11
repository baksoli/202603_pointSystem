package com.payments.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "point_usage_details")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointUsageDetail extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private PointTransaction transactionId; // 사용 트랜잭션 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Point pointId; // 사용된 원천 포인트 ID

    @JoinColumn(name = "used_amount")
    private Long usedAmount; // 차감된 금액
}