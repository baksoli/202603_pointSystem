package com.payments.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Point extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;


    @Column(name = "earn_transaction_id")
    private Long earnTransactionId; // 적립 시 발생한 트랜잭션 ID

    @Enumerated(EnumType.STRING)
    private PointType type; // NORMAL, ADMIN_MANUAL (수기 지급 식별용)

    private Long initialAmount; // 최초 적립액
    private Long remainAmount;  // 현재 남은 잔액

    @Setter
    private LocalDateTime expiredAt; // 만료일 (최소 1일~최대 5년)

    public enum PointType { NORMAL, ADMIN_MANUAL, REISSUE }

    // 포인트 사용 시: 남은 잔액 차감
    public void use(Long amount) {
        if (this.remainAmount < amount) {
            throw new IllegalArgumentException("사용하려는 금액이 남은 원천 포인트보다 큽니다.");
        }
        this.remainAmount -= amount;
    }

    // 사용 취소 시: 차감됐던 잔액 다시 복구
    public void cancelUse(Long amount) {
        this.remainAmount += amount;
    }

    // 적립 취소 시: 사용된 적이 있는지 확인
    public boolean isNotUsed() {
        return this.initialAmount.equals(this.remainAmount);
    }

    /**
     * 현재 이 포인트가 사용 가능한 상태인지 확인 (잔액 > 0 && 만료 전)
     */
    public boolean isAvailable(LocalDateTime now) {
        return this.remainAmount > 0 && this.expiredAt.isAfter(now); //
    }

}