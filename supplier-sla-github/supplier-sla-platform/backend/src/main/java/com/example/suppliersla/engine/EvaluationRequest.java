package com.example.suppliersla.engine;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EvaluationRequest(
    @NotBlank String supplierName,
    @NotBlank String month,
    @DecimalMin("0.0") BigDecimal otifTargetPercent,
    @Min(0) int graceDays,
    @NotNull @DecimalMin("0.0") BigDecimal monthlyOrderValue,
    @NotNull @DecimalMin("0.0") BigDecimal monthlyCapPercent,
    @NotNull @DecimalMin("0.0") BigDecimal penaltyRatePerDay,
    @NotBlank String penaltyBasis,
    @NotEmpty List<@Valid OrderInput> orders
) {
    public record OrderInput(
        @NotBlank String orderNumber,
        @NotNull @DecimalMin("0.0") BigDecimal orderValue,
        @NotNull @DecimalMin("0.001") BigDecimal orderedQuantity,
        @NotNull LocalDate promisedDate,
        @Min(0) int approvedExtensionDays,
        List<@Valid ReceiptInput> receipts
    ) {}

    public record ReceiptInput(
        @NotNull LocalDate receiptDate,
        @NotNull @DecimalMin("0.0") BigDecimal acceptedQuantity,
        @NotNull @DecimalMin("0.0") BigDecimal disputedQuantity
    ) {}
}
