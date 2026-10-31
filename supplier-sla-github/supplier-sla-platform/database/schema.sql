CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS supplier_locations (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    location_name VARCHAR(200) NOT NULL,
    timezone VARCHAR(80) NOT NULL DEFAULT 'Asia/Kolkata'
);

CREATE TABLE IF NOT EXISTS agreements (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    agreement_number VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    UNIQUE (supplier_id, agreement_number)
);

CREATE TABLE IF NOT EXISTS agreement_versions (
    id BIGSERIAL PRIMARY KEY,
    agreement_id BIGINT NOT NULL REFERENCES agreements(id),
    version_number INTEGER NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (agreement_id, version_number)
);

CREATE TABLE IF NOT EXISTS agreement_sla_rules (
    id BIGSERIAL PRIMARY KEY,
    agreement_version_id BIGINT NOT NULL UNIQUE REFERENCES agreement_versions(id),
    otif_target_percent NUMERIC(7,4) NOT NULL,
    grace_days INTEGER NOT NULL,
    grace_unit VARCHAR(30) NOT NULL DEFAULT 'CALENDAR_DAYS',
    penalty_threshold_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS agreement_penalty_rules (
    id BIGSERIAL PRIMARY KEY,
    agreement_version_id BIGINT NOT NULL REFERENCES agreement_versions(id),
    formula_type VARCHAR(60) NOT NULL,
    daily_rate NUMERIC(12,8),
    fixed_rate NUMERIC(12,8),
    fixed_amount NUMERIC(18,2),
    penalty_basis VARCHAR(60) NOT NULL,
    cap_percent NUMERIC(7,4) NOT NULL,
    cap_basis VARCHAR(60) NOT NULL,
    rounding_mode VARCHAR(30) NOT NULL DEFAULT 'HALF_UP'
);

CREATE TABLE IF NOT EXISTS agreement_exception_rules (
    id BIGSERIAL PRIMARY KEY,
    agreement_version_id BIGINT NOT NULL REFERENCES agreement_versions(id),
    event_type VARCHAR(60) NOT NULL,
    extension_method VARCHAR(60) NOT NULL,
    max_extension_days INTEGER,
    approval_required BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS holiday_calendars (
    id BIGSERIAL PRIMARY KEY,
    calendar_name VARCHAR(150) NOT NULL,
    calendar_type VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS holiday_dates (
    id BIGSERIAL PRIMARY KEY,
    calendar_id BIGINT NOT NULL REFERENCES holiday_calendars(id),
    holiday_date DATE NOT NULL,
    description VARCHAR(250),
    UNIQUE(calendar_id, holiday_date)
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(80) NOT NULL,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    agreement_version_id BIGINT NOT NULL REFERENCES agreement_versions(id),
    order_value NUMERIC(18,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    ordered_quantity NUMERIC(18,3) NOT NULL,
    promised_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(supplier_id, po_number),
    CHECK(order_value >= 0),
    CHECK(ordered_quantity > 0)
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id),
    material_code VARCHAR(100) NOT NULL,
    ordered_quantity NUMERIC(18,3) NOT NULL,
    unit_price NUMERIC(18,4) NOT NULL
);

CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id),
    shipment_reference VARCHAR(100) NOT NULL,
    dispatch_date DATE,
    shipped_quantity NUMERIC(18,3) NOT NULL,
    UNIQUE(purchase_order_id, shipment_reference),
    CHECK(shipped_quantity >= 0)
);

CREATE TABLE IF NOT EXISTS goods_receipts (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id),
    grn_number VARCHAR(100) NOT NULL UNIQUE,
    receipt_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECORDED'
);

CREATE TABLE IF NOT EXISTS goods_receipt_items (
    id BIGSERIAL PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL REFERENCES goods_receipts(id),
    purchase_order_item_id BIGINT NOT NULL REFERENCES purchase_order_items(id),
    physically_received NUMERIC(18,3) NOT NULL,
    accepted_quantity NUMERIC(18,3) NOT NULL DEFAULT 0,
    rejected_quantity NUMERIC(18,3) NOT NULL DEFAULT 0,
    disputed_quantity NUMERIC(18,3) NOT NULL DEFAULT 0,
    CHECK(physically_received >= 0),
    CHECK(accepted_quantity >= 0 AND rejected_quantity >= 0 AND disputed_quantity >= 0),
    CHECK(accepted_quantity + rejected_quantity + disputed_quantity <= physically_received)
);

CREATE TABLE IF NOT EXISTS exception_cases (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id),
    exception_type VARCHAR(50) NOT NULL,
    event_start DATE NOT NULL,
    event_end DATE NOT NULL,
    requested_extension_days INTEGER NOT NULL DEFAULT 0,
    approved_extension_days INTEGER,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exception_approvals (
    id BIGSERIAL PRIMARY KEY,
    exception_case_id BIGINT NOT NULL REFERENCES exception_cases(id),
    approver_user_id BIGINT,
    decision VARCHAR(30) NOT NULL,
    comments TEXT,
    decided_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(60) NOT NULL,
    storage_reference TEXT NOT NULL,
    original_filename VARCHAR(255),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evaluation_batches (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    reporting_month CHAR(7) NOT NULL,
    revision INTEGER NOT NULL DEFAULT 1,
    cutoff_date DATE NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(supplier_id, reporting_month, revision)
);

CREATE TABLE IF NOT EXISTS sla_evaluations (
    id BIGSERIAL PRIMARY KEY,
    evaluation_batch_id BIGINT NOT NULL REFERENCES evaluation_batches(id),
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id),
    original_deadline DATE NOT NULL,
    adjusted_deadline DATE NOT NULL,
    completion_date DATE,
    accepted_quantity NUMERIC(18,3) NOT NULL,
    outstanding_quantity NUMERIC(18,3) NOT NULL,
    delay_days INTEGER NOT NULL DEFAULT 0,
    decision VARCHAR(40) NOT NULL,
    explanation TEXT,
    UNIQUE(evaluation_batch_id, purchase_order_id)
);

CREATE TABLE IF NOT EXISTS penalty_assessments (
    id BIGSERIAL PRIMARY KEY,
    sla_evaluation_id BIGINT NOT NULL REFERENCES sla_evaluations(id),
    gross_amount NUMERIC(18,2) NOT NULL,
    final_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    calculation_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(80) NOT NULL,
    actor_reference VARCHAR(120),
    before_state JSONB,
    after_state JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
