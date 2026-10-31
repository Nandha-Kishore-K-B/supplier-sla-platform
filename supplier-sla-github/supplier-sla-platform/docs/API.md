# API Reference

Base URL: `/api/v1`

## Health
`GET /health`

## Demo monthly report
`GET /demo/monthly-report?month=2026-10`

Returns supplier summaries, order decisions and recommended penalties.

## Demo orders
`GET /demo/orders`

## Order explanation
`GET /demo/orders/{orderNumber}/explanation`

## Evaluation preview
`POST /evaluations/preview`

Example:
```json
{
  "supplierName": "Example Supplier",
  "month": "2026-10",
  "otifTargetPercent": 95,
  "graceDays": 2,
  "monthlyOrderValue": 700000,
  "monthlyCapPercent": 5,
  "penaltyRatePerDay": 0.005,
  "penaltyBasis": "FULL_PO_VALUE",
  "orders": [
    {
      "orderNumber": "PO-1",
      "orderValue": 100000,
      "orderedQuantity": 1000,
      "promisedDate": "2026-10-07",
      "approvedExtensionDays": 0,
      "receipts": [
        {"receiptDate":"2026-10-09","acceptedQuantity":1000,"disputedQuantity":0}
      ]
    }
  ]
}
```
