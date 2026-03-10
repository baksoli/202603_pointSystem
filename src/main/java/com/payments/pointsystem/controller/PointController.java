package com.payments.pointsystem.controller;

import com.payments.pointsystem.domain.Point;
import com.payments.pointsystem.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 1. 포인트 적립 (Add)
     * POST /api/v1/points/adds?userSeq=1&amount=1000&type=NORMAL
     */
    @PostMapping("/adds")
    public ResponseEntity<Long> addPoint(@RequestParam Long userSeq,
                                         @RequestParam Long amount,
                                         @RequestParam Point.PointType type) {
        Long transactionId = pointService.addPoint(userSeq, amount, type);
        return ResponseEntity.ok(transactionId);
    }

    /**
     * 2. 포인트 사용 (Use)
     * POST /api/v1/points/uses?userSeq=1&amount=500&orderNo=ORD-001
     */
    @PostMapping("/uses")
    public ResponseEntity<Long> usePoint(@RequestParam Long userSeq,
                                         @RequestParam Long amount,
                                         @RequestParam String orderNo) {
        Long transactionId = pointService.usePoint(userSeq, amount, orderNo);
        return ResponseEntity.ok(transactionId);
    }

    /**
     * 3. 적립 취소 (Cancel Earn)
     * POST /api/v1/points/adds/{transactionId}/cancel
     */
    @PostMapping("/adds/{transactionId}/cancel")
    public ResponseEntity<Void> cancelEarn(@PathVariable Long transactionId) {
        pointService.cancelEarn(transactionId);
        return ResponseEntity.ok().build();
    }

    /**
     * 4. 사용 취소 (Cancel Use)
     * POST /api/v1/points/uses/{transactionId}/cancel
     */
    @PostMapping("/uses/{transactionId}/cancel")
    public ResponseEntity<Void> cancelUse(@PathVariable Long transactionId, @RequestParam Long cancelAmount) {
        pointService.cancelUse(transactionId, cancelAmount);
        return ResponseEntity.ok().build();
    }
}