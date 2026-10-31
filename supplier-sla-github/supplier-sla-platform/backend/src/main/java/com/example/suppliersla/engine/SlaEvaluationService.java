package com.example.suppliersla.engine;

import org.springframework.stereotype.Service;

import java.math.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SlaEvaluationService {
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public EvaluationResponse evaluate(EvaluationRequest request) {
        YearMonth month = YearMonth.parse(request.month());
        List<EvaluationResponse.OrderResult> results = new ArrayList<>();
        int compliant = 0;
        int failed = 0;
        BigDecimal grossPenalty = BigDecimal.ZERO;

        for (EvaluationRequest.OrderInput order : request.orders()) {
            if (!YearMonth.from(order.promisedDate()).equals(month)) continue;

            LocalDate originalDeadline = order.promisedDate().plusDays(request.graceDays());
            LocalDate adjustedDeadline = originalDeadline.plusDays(order.approvedExtensionDays());

            BigDecimal accepted = BigDecimal.ZERO;
            LocalDate completion = null;
            List<EvaluationRequest.ReceiptInput> receipts = new ArrayList<>(
                Optional.ofNullable(order.receipts()).orElse(List.of())
            );
            receipts.sort(Comparator.comparing(EvaluationRequest.ReceiptInput::receiptDate));

            for (var receipt : receipts) {
                accepted = accepted.add(receipt.acceptedQuantity());
                if (completion == null && accepted.compareTo(order.orderedQuantity()) >= 0) {
                    completion = receipt.receiptDate();
                }
            }

            BigDecimal recognized = accepted.min(order.orderedQuantity());
            BigDecimal outstanding = order.orderedQuantity().subtract(recognized).max(BigDecimal.ZERO);
            boolean complete = outstanding.compareTo(BigDecimal.ZERO) == 0;
            long delayDays = complete && completion != null && completion.isAfter(adjustedDeadline)
                ? ChronoUnit.DAYS.between(adjustedDeadline, completion) : 0;

            String status;
            BigDecimal penalty = BigDecimal.ZERO;
            String explanation;

            if (complete && completion != null && !completion.isAfter(adjustedDeadline)) {
                status = "COMPLIANT";
                compliant++;
                explanation = "100% accepted quantity was completed on or before the adjusted deadline.";
            } else {
                failed++;
                if (!complete) {
                    status = "SHORT_AT_CUTOFF";
                    long provisionalDays = Math.max(0, ChronoUnit.DAYS.between(
                        adjustedDeadline, month.atEndOfMonth()));
                    BigDecimal outstandingValue = order.orderValue()
                        .multiply(outstanding)
                        .divide(order.orderedQuantity(), 8, RoundingMode.HALF_UP);
                    if ("DELAYED_QUANTITY_PER_DAY".equals(request.penaltyBasis())) {
                        penalty = outstandingValue.multiply(request.penaltyRatePerDay())
                            .multiply(BigDecimal.valueOf(provisionalDays));
                    } else {
                        penalty = outstandingValue.multiply(request.penaltyRatePerDay())
                            .multiply(BigDecimal.valueOf(provisionalDays));
                    }
                    delayDays = provisionalDays;
                    explanation = "Order remained short at month-end. " + outstanding.stripTrailingZeros().toPlainString()
                        + " units remain outstanding; assessment is provisional through "
                        + month.atEndOfMonth() + ".";
                } else {
                    status = "LATE";
                    BigDecimal base = order.orderValue();
                    if ("DELAYED_QUANTITY_PER_DAY".equals(request.penaltyBasis())) {
                        base = order.orderValue();
                    }
                    penalty = base.multiply(request.penaltyRatePerDay())
                        .multiply(BigDecimal.valueOf(delayDays));
                    explanation = "Complete accepted quantity was received " + delayDays
                        + " calendar day(s) after the adjusted deadline.";
                }
                grossPenalty = grossPenalty.add(penalty);
            }

            results.add(new EvaluationResponse.OrderResult(
                order.orderNumber(), originalDeadline, adjustedDeadline, completion,
                recognized, outstanding, delayDays, status, money(penalty), explanation
            ));
        }

        int eligible = compliant + failed;
        BigDecimal otif = eligible == 0 ? BigDecimal.ZERO :
            BigDecimal.valueOf(compliant).multiply(HUNDRED)
                .divide(BigDecimal.valueOf(eligible), 4, RoundingMode.HALF_UP);

        boolean breached = eligible > 0 && BigDecimal.valueOf(compliant)
            .multiply(HUNDRED)
            .compareTo(request.otifTargetPercent().multiply(BigDecimal.valueOf(eligible))) < 0;

        BigDecimal cap = request.monthlyOrderValue()
            .multiply(request.monthlyCapPercent())
            .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal recommended = breached ? grossPenalty.min(cap) : BigDecimal.ZERO;

        return new EvaluationResponse(
            request.supplierName(), request.month(), eligible, compliant, failed,
            otif, breached, money(grossPenalty), money(cap), money(recommended), results
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
