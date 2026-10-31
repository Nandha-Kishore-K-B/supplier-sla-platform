# Business Rules — Baseline

1. Monthly cohort: purchase orders are assigned to the month containing their promised date.
2. Grace period: two calendar days; the final grace day is inclusive.
3. Completion: 100% accepted quantity evidenced by GRN.
4. Partial deliveries: the applicable supplier agreement determines the penalty base.
5. Short at cutoff: mark `SHORT_AT_CUTOFF` and calculate a provisional assessment using outstanding quantity where the agreement supports that formula.
6. Disputed quantities: exclude from accepted quantity until resolved; provisional assessment may be produced.
7. Force majeure: only authorized approval can grant an extension. The agreement defines eligible days, maximum extension and overlap handling.
8. Holidays: supplier and receiving-location calendars are distinct. Baseline grace days remain calendar days.
9. Threshold: penalties activate only when exact OTIF ratio is below 95%; exactly 95% does not activate.
10. Penalty: use an agreement-configured formula and cap basis.
11. Monthly cap: aggregate eligible penalties and apply the configured supplier/agreement cap.
12. Re-evaluation: create a new evaluation revision and preserve the prior snapshot; never silently overwrite an approved assessment.
13. Financial values: use decimal arithmetic (`BigDecimal`) and agreement-specific rounding rules.
14. AI: extraction and explanation are assistive only. Deterministic engine outputs remain authoritative.

## Decisions requiring contract-by-contract confirmation
- Whether failed orders attract penalties only after monthly threshold breach or regardless of threshold.
- Penalty base for partial deliveries.
- Short-delivery provisional period and final reconciliation method.
- Force-majeure eligible-day counting and overlapping events.
- Whether cap is per agreement or consolidated supplier-wide.
- Exact cap basis, currency, rounding and tax treatment.
