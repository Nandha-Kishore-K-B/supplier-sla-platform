package com.example.suppliersla.engine;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SlaEvaluationServiceTest {
    private final SlaEvaluationService service = new SlaEvaluationService();

    @Test
    void deliveryOnLastGraceDayIsCompliant() {
        var order = new EvaluationRequest.OrderInput(
            "PO-1", new BigDecimal("100000"), new BigDecimal("1000"),
            LocalDate.parse("2026-10-07"), 0,
            List.of(new EvaluationRequest.ReceiptInput(
                LocalDate.parse("2026-10-09"), new BigDecimal("1000"), BigDecimal.ZERO
            ))
        );
        var request = new EvaluationRequest(
            "Supplier", "2026-10", new BigDecimal("95"), 2,
            new BigDecimal("100000"), new BigDecimal("5"),
            new BigDecimal("0.005"), "FULL_PO_VALUE", List.of(order)
        );
        var result = service.evaluate(request);
        assertEquals(1, result.compliantOrders());
        assertFalse(result.thresholdBreached());
    }

    @Test
    void exactNinetyFivePercentDoesNotActivatePenalties() {
        var orders = new java.util.ArrayList<EvaluationRequest.OrderInput>();
        for (int i = 0; i < 20; i++) {
            LocalDate promised = LocalDate.parse("2026-10-01").plusDays(i);
            LocalDate receipt = i < 19 ? promised.plusDays(1) : promised.plusDays(4);
            orders.add(new EvaluationRequest.OrderInput(
                "PO-" + i, new BigDecimal("1000"), new BigDecimal("10"),
                promised, 0,
                List.of(new EvaluationRequest.ReceiptInput(receipt,
                    new BigDecimal("10"), BigDecimal.ZERO))
            ));
        }
        var request = new EvaluationRequest("Supplier", "2026-10",
            new BigDecimal("95"), 2, new BigDecimal("20000"),
            new BigDecimal("5"), new BigDecimal("0.005"),
            "FULL_PO_VALUE", orders);
        var result = service.evaluate(request);
        assertEquals(95, result.otifPercentage().doubleValue(), 0.0001);
        assertFalse(result.thresholdBreached());
        assertEquals(0, result.recommendedPenalty().compareTo(BigDecimal.ZERO));
    }
}
