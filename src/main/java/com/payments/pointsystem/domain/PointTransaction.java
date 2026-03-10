package com.payments.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointTransaction extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // EARN, USE, CANCEL 등

    private Long amount;
    private String orderNo;

    public enum TransactionType { EARN, EARN_CANCEL, USE, USE_CANCEL }
}