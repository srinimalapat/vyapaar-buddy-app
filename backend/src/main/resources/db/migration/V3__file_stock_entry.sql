-- File Stock Entry tables — extends Phase 10 to support images + documents

CREATE TABLE IF NOT EXISTS file_stock_entries (
    id                   BIGSERIAL PRIMARY KEY,
    business_id          BIGINT        NOT NULL REFERENCES businesses(id),
    uploaded_by_user_id  BIGINT        NOT NULL REFERENCES users(id),
    source_type          VARCHAR(50)   NOT NULL CHECK (source_type IN ('LOCAL_UPLOAD', 'WHATSAPP_MEDIA')),
    file_type            VARCHAR(50)   NOT NULL CHECK (file_type IN ('IMAGE','PDF','EXCEL','CSV','WORD','TEXT','UNKNOWN')),
    original_file_name   VARCHAR(255),
    stored_file_path     VARCHAR(500),
    content_type         VARCHAR(150),
    file_size            BIGINT,
    extracted_text       TEXT,
    status               VARCHAR(50)   NOT NULL CHECK (status IN ('PENDING_REVIEW','CONFIRMED','CANCELLED','FAILED')),
    error_message        TEXT,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fse_business_id ON file_stock_entries (business_id);
CREATE INDEX IF NOT EXISTS idx_fse_status      ON file_stock_entries (status);
CREATE INDEX IF NOT EXISTS idx_fse_file_type   ON file_stock_entries (file_type);
CREATE INDEX IF NOT EXISTS idx_fse_created_at  ON file_stock_entries (created_at);

CREATE TABLE IF NOT EXISTS file_stock_entry_items (
    id                    BIGSERIAL PRIMARY KEY,
    file_stock_entry_id   BIGINT          NOT NULL REFERENCES file_stock_entries(id),
    item_name             VARCHAR(255)    NOT NULL,
    quantity              NUMERIC(15, 3),
    unit                  VARCHAR(50),
    unit_price            NUMERIC(15, 2),
    category              VARCHAR(100)    DEFAULT 'General',
    confidence_score      NUMERIC(5, 2),
    validation_errors     TEXT,
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fsei_entry_id ON file_stock_entry_items (file_stock_entry_id);
