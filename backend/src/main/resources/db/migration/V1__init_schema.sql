-- Vyapaar Buddy initial schema — PostgreSQL

CREATE TABLE users (
    id                      BIGSERIAL PRIMARY KEY,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password                VARCHAR(255) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    phone                   VARCHAR(20),
    role                    VARCHAR(50)  NOT NULL,
    enabled                 BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_expired     BOOLEAN      NOT NULL DEFAULT TRUE,
    account_non_locked      BOOLEAN      NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE businesses (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users (id),
    name               VARCHAR(255) NOT NULL,
    type               VARCHAR(50),
    description        TEXT,
    address            TEXT,
    city               VARCHAR(100),
    state              VARCHAR(100),
    pin_code           VARCHAR(10),
    gst_number         VARCHAR(50),
    phone              VARCHAR(20),
    preferred_language VARCHAR(50),
    timezone           VARCHAR(50),
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
    id             BIGSERIAL PRIMARY KEY,
    business_id    BIGINT          NOT NULL REFERENCES businesses (id),
    name           VARCHAR(255)    NOT NULL,
    phone          VARCHAR(20)     UNIQUE,
    email          VARCHAR(255),
    address        TEXT,
    status         VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',
    notes          TEXT,
    credit_balance DECIMAL(15, 2)           DEFAULT 0.00,
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP                DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_items (
    id                  BIGSERIAL PRIMARY KEY,
    business_id         BIGINT          NOT NULL REFERENCES businesses (id),
    name                VARCHAR(255)    NOT NULL,
    sku                 VARCHAR(100)    UNIQUE,
    description         TEXT,
    category            VARCHAR(100),
    unit_price          DECIMAL(15, 2)  NOT NULL,
    quantity            INTEGER         NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER         NOT NULL DEFAULT 5,
    unit                VARCHAR(50),
    status              VARCHAR(50)     NOT NULL DEFAULT 'IN_STOCK',
    supplier            VARCHAR(255),
    cost_price          DECIMAL(15, 2),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP                DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales (
    id              BIGSERIAL PRIMARY KEY,
    business_id     BIGINT          NOT NULL REFERENCES businesses (id),
    customer_id     BIGINT          NOT NULL REFERENCES customers (id),
    sale_date       DATE            NOT NULL,
    type            VARCHAR(50),
    total_amount    DECIMAL(15, 2)  NOT NULL,
    discount_amount DECIMAL(15, 2)           DEFAULT 0.00,
    tax_amount      DECIMAL(15, 2)           DEFAULT 0.00,
    paid_amount     DECIMAL(15, 2)           DEFAULT 0.00,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sale_items (
    id                BIGSERIAL PRIMARY KEY,
    sale_id           BIGINT         NOT NULL REFERENCES sales (id),
    inventory_item_id BIGINT         NOT NULL REFERENCES inventory_items (id),
    quantity          INTEGER        NOT NULL,
    unit_price        DECIMAL(15, 2) NOT NULL,
    discount_amount   DECIMAL(15, 2)          DEFAULT 0.00,
    total_price       DECIMAL(15, 2),
    description       TEXT,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE credit_transactions (
    id               BIGSERIAL PRIMARY KEY,
    business_id      BIGINT         NOT NULL REFERENCES businesses (id),
    customer_id      BIGINT         NOT NULL REFERENCES customers (id),
    type             VARCHAR(50)    NOT NULL,
    amount           DECIMAL(15, 2) NOT NULL,
    transaction_date DATE           NOT NULL,
    due_date         DATE,
    description      TEXT,
    notes            TEXT,
    is_settled       BOOLEAN        NOT NULL DEFAULT FALSE,
    settled_date     DATE,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reminders (
    id                    BIGSERIAL PRIMARY KEY,
    business_id           BIGINT         NOT NULL REFERENCES businesses (id),
    customer_id           BIGINT         NOT NULL REFERENCES customers (id),
    credit_transaction_id BIGINT         NOT NULL REFERENCES credit_transactions (id),
    amount                DECIMAL(15, 2) NOT NULL,
    due_date              DATE,
    scheduled_date        TIMESTAMP,
    sent_date             TIMESTAMP,
    channel               VARCHAR(50),
    status                VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    message               TEXT,
    notes                 TEXT,
    retry_count           INTEGER                 DEFAULT 0,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

-- Default roles
INSERT INTO roles (name, description)
VALUES ('ADMIN', 'Administrator with full access'),
       ('USER', 'Regular business user'),
       ('VIEWER', 'Read-only access');

-- Indexes for common query patterns
CREATE INDEX idx_businesses_user_id ON businesses (user_id);
CREATE INDEX idx_customers_business_id ON customers (business_id);
CREATE INDEX idx_customers_phone ON customers (phone);
CREATE INDEX idx_sales_business_id ON sales (business_id);
CREATE INDEX idx_sales_customer_id ON sales (customer_id);
CREATE INDEX idx_sales_sale_date ON sales (sale_date);
CREATE INDEX idx_sale_items_sale_id ON sale_items (sale_id);
CREATE INDEX idx_credit_transactions_business_id ON credit_transactions (business_id);
CREATE INDEX idx_credit_transactions_customer_id ON credit_transactions (customer_id);
CREATE INDEX idx_reminders_business_id ON reminders (business_id);
CREATE INDEX idx_reminders_status ON reminders (status);
CREATE INDEX idx_inventory_items_business_id ON inventory_items (business_id);
