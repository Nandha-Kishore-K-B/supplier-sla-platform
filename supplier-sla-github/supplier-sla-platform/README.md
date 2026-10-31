# Supplier SLA Compliance & Penalty Management Platform

A runnable full-stack MVP starter implementing the agreed Supplier SLA workflow.

## Included
- Java 21 + Spring Boot REST API
- React + TypeScript + Vite frontend
- PostgreSQL schema and Docker Compose service
- Deterministic demo evaluation for 3 suppliers / 20 orders
- OTIF cohort based on promised date month
- Inclusive 2-calendar-day grace period
- Accepted-GRN quantity completion
- Approved force-majeure extension fields
- Agreement-configurable penalty basis and monthly cap
- Provisional short-delivery assessment at cutoff
- Monthly report and order-level explanation endpoints
- CSV import templates and API documentation

## Important scope note
This is a runnable MVP foundation, not a production-certified procurement or financial settlement system. The demo dataset and demo rules are illustrative. Before using real supplier contracts, configure and legally validate each agreement's exact threshold trigger, penalty formula, cap basis, rounding, force-majeure treatment, receipt reversals, cohort eligibility and month-end reconciliation rules. The included database schema is a foundation; the demo evaluation currently uses seeded in-memory data rather than persisting transactions to PostgreSQL.

## Requirements
- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Desktop (optional, for PostgreSQL)

## Run locally

### 1. Start PostgreSQL (optional for this demo API)
```bash
docker compose up -d postgres
```

### 2. Start backend
```bash
cd backend
mvn spring-boot:run
```
Backend: http://localhost:8080

### 3. Start frontend
In another terminal:
```bash
cd frontend
npm install
npm run dev
```
Frontend: http://localhost:5173

The frontend proxies `/api` requests to the backend.

## Main API
- `GET /api/v1/health`
- `GET /api/v1/demo/monthly-report?month=2026-10`
- `GET /api/v1/demo/orders`
- `GET /api/v1/demo/orders/{orderNumber}/explanation`
- `POST /api/v1/evaluations/preview` (evaluate a supplied payload)

## Demo assumptions
- The monthly OTIF cohort is based on promised date month.
- Grace period is 2 calendar days and the final grace day is inclusive.
- OTIF requires 100% accepted quantity by the adjusted deadline.
- Monthly penalties activate only when unrounded OTIF is strictly below 95%.
- The sample agreement penalty formula is daily percentage on full PO value.
- Force majeure approval and extension are represented in the seeded demo data; real claim evidence/approval workflow must be connected before production.
- A short order is assessed provisionally at the cutoff using outstanding quantity value and provisional delay days, when the agreement uses delayed-quantity-per-day.
- Holiday calendars are stored in the schema; baseline grace periods are calendar-day based and therefore do not skip holidays.

## Project structure
```text
backend/
  src/main/java/com/example/suppliersla/
  src/main/resources/
frontend/
  src/
database/
  schema.sql
  seed-reference.sql
templates/
  supplier-template.csv
  agreement-template.csv
  purchase-orders-template.csv
  shipments-template.csv
  goods-receipts-template.csv
docs/
  BUSINESS_RULES.md
  API.md
```
