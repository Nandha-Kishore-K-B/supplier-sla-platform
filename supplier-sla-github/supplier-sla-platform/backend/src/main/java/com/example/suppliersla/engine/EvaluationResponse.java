package com.example.suppliersla.engine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EvaluationResponse(
    String supplierName,
    String month,
    int eligibleOrders,
    int compliantOrders,
    int failedOrders,
    BigDecimal otifPercentage,
    boolean thresholdBreached,
    BigDecimal calculatedPenalty,
    BigDecimal monthlyCap,
    BigDecimal recommendedPenalty,
    List<OrderResult> orders
) {
    public record OrderResult(
        String orderNumber,
        LocalDate originalDeadline,
        LocalDate adjustedDeadline,
        LocalDate completionDate,
        BigDecimal acceptedQuantity,
        BigDecimal outstandingQuantity,
        long delayDays,
        String status,
        BigDecimal penalty,
        String explanation
    ) {}
}
