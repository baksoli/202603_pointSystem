package com.payments.pointsystem.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_point", nullable = false)
    private Long totalPoint = 0L;

    // 포인트 합산 (적립 시 사용)
    public void addPoint(Long amount) {
        this.totalPoint += amount;
    }

    // 포인트 차감 (사용 시 사용)
    public void deductPoint(Long amount) {
        if (this.totalPoint < amount) {
            throw new IllegalArgumentException("잔여 포인트가 부족합니다.");
        }
        this.totalPoint -= amount;
    }
}