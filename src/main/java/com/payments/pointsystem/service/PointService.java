package com.payments.pointsystem.service;

import com.payments.pointsystem.domain.*;
import com.payments.pointsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointTransactionRepository transactionRepository;
    private final PointPolicyRepository policyRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;

    /**
     * 1. 포인트 적립 (Add)
     */
    public Long addPoint(Long userSeq, Long amount, Point.PointType type) {
        User user = userRepository.findByIdWithLock(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        validateAddPolicy(amount);
        PointTransaction transaction = PointTransaction.builder()
                .userId(user)
                .type(PointTransaction.TransactionType.EARN)
                .amount(amount)
                .build();
        transactionRepository.save(transaction); // 여기서 ID가 발급됩니다.

        Point point = Point.builder()
                .userId(user)
                .type(type)
                .earnTransactionId(transaction.getTransactionId()) // 발급된 ID 연결
                .initialAmount(amount)
                .remainAmount(amount)
                .expiredAt(LocalDateTime.now().plusDays(365))
                .build();
        pointRepository.save(point);

        user.addPoint(amount);
        return transaction.getTransactionId();
    }

    /**
     * 2. 적립 취소 (Cancel Earn)
     */
    public void cancelEarn(Long transactionId) {
        // 1. 트랜잭션 조회
        PointTransaction originTx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랜잭션입니다."));

        // 2. earnTransactionId로 포인트 조회
        Point point = pointRepository.findByEarnTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("원본 포인트 데이터를 찾을 수 없습니다."));

        // 3. 사용 여부 체크
        if (!point.isNotUsed()) {
            throw new IllegalStateException("이미 사용된 포인트는 적립 취소가 불가능합니다.");
        }

        // 4. 취소 트랜잭션 기록
        PointTransaction cancelTx = PointTransaction.builder()
                .userId(originTx.getUserId())
                .type(PointTransaction.TransactionType.EARN_CANCEL)
                .amount(originTx.getAmount())
                .build();
        transactionRepository.save(cancelTx);

        // 5. 유저 잔액 및 포인트 무효화
        point.use(point.getRemainAmount());
        originTx.getUserId().deductPoint(originTx.getAmount());
    }

    /**
     * 3. 포인트 사용 (Use)
     */
    public Long usePoint(Long userSeq, Long amount, String orderNo) {
        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getTotalPoint() < amount) {
            throw new IllegalArgumentException("잔액 부족");
        }

        List<Point> availablePoints = pointRepository.findAvailablePoints(user, LocalDateTime.now());

        PointTransaction useTransaction = PointTransaction.builder()
                .userId(user)
                .type(PointTransaction.TransactionType.USE)
                .amount(amount)
                .orderNo(orderNo)
                .build();
        transactionRepository.save(useTransaction);

        Long remainToUse = amount;
        for (Point point : availablePoints) {
            if (remainToUse <= 0) break;

            if (!point.isAvailable(LocalDateTime.now())) {
                continue;
            }
            
            long canUseAmount = Math.min(point.getRemainAmount(), remainToUse); // remainAmount 사용

            point.use(canUseAmount);
            remainToUse -= canUseAmount;

            PointUsageDetail detail = PointUsageDetail.builder()
                    .transactionId(useTransaction)
                    .pointId(point)
                    .usedAmount(canUseAmount)
                    .build();
            pointUsageDetailRepository.save(detail);
        }

        user.deductPoint(amount);
        return useTransaction.getTransactionId();
    }

    /**
     * 4.사용 취소 (Cancel Use)
     */
    @Transactional
    public void cancelUse(Long transactionId, Long cancelAmount) {
        PointTransaction originTx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("사용 이력을 찾을 수 없습니다."));
        List<PointUsageDetail> details = pointUsageDetailRepository.findByTransactionId(originTx);

        long remainToCancel = cancelAmount;
        LocalDateTime now = LocalDateTime.now();
        User user = originTx.getUserId(); // 유저 객체 미리 꺼내기

        // 3. 상세 내역을 돌면서 복구 진행
        for (PointUsageDetail detail : details) {
            if (remainToCancel <= 0) break;

            Point originPoint = detail.getPointId();
            long actualCancelAmount = Math.min(detail.getUsedAmount(), remainToCancel);

            if (originPoint.getExpiredAt().isBefore(now)) {
                // [Case A] 만료됨 -> 재발급
                // addPoint 메서드가 유저의 totalPoint
                addPoint(user.getUserId(), actualCancelAmount, Point.PointType.REISSUE);
            } else {
                // [Case B] 유효함 -> 기존 포인트 잔액 복구
                originPoint.cancelUse(actualCancelAmount);
                // 직접 유저 잔액을 올려줍니다.
                user.addPoint(actualCancelAmount);
            }

            remainToCancel -= actualCancelAmount;
        }

        // 4. 취소 트랜잭션 기록
        PointTransaction cancelTx = PointTransaction.builder()
                .userId(user)
                .type(PointTransaction.TransactionType.USE_CANCEL)
                .amount(cancelAmount)
                .orderNo(originTx.getOrderNo())
                .build();
        transactionRepository.save(cancelTx);
    }

    private void validateAddPolicy(Long amount) {
        PointPolicy addLimit = policyRepository.findByPolicyName("POINT_ADD_LIMIT")
                .orElseThrow(() -> new IllegalStateException("적립 정책이 설정되지 않았습니다."));

        if (amount < addLimit.getMinValue() || amount > addLimit.getMaxValue()) {
            throw new IllegalArgumentException(
                    String.format("적립 가능 금액은 %d원 ~ %d원 사이여야 합니다.",
                            addLimit.getMinValue(), addLimit.getMaxValue())
            );
        }
    }
}