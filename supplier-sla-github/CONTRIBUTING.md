# Contributing

Thanks for contributing!

## Development guidelines
- Use Java 21 and the existing Spring Boot conventions.
- Keep business rules in domain services, not REST controllers.
- Use `BigDecimal` for financial calculations.
- Add unit tests for deadline, OTIF, penalty, cap and exception behavior.
- Preserve approved evaluation snapshots; create revisions for recalculation.
- Update `docs/BUSINESS_RULES.md` and `docs/API.md` when behavior changes.

## Pull requests
Include:
1. Problem and proposed change.
2. Business rule affected.
3. Tests run and results.
4. Screenshots for user-interface changes.
