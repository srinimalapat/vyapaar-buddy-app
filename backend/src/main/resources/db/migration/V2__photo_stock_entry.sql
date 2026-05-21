-- Photo Stock Entry tables for image-based stock upload feature

CREATE TABLE IF NOT EXISTS photo_stock_entries (
    id                   BIGSERIAL PRIMARY KEY,
    business_id          BIGINT        NOT NULL REFERENCES businesses(id),
    uploaded_by_user_id  BIGINT        NOT NULL REFERENCES users(id),
    source_type          VARCHAR(50)   NOT NULL CHECK (source_type IN ('LOCAL_UPLOAD', 'WHATSAPP_MEDIA')),
    original_file_name   VARCHAR(255),
    stored_file_path     VARCHAR(500),
    content_type         VARCHAR(100),
    file_size            BIGINT,
    extracted_text       TEXT,
    status               VARCHAR(50)   NOT NULL CHECK (status IN ('PENDING_REVIEW', 'CONFIRMED', 'CANCELLED', 'FAILED')),
    error_message        TEXT,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pse_business_id  ON photo_stock_entries (business_id);
CREATE INDEX IF NOT EXISTS idx_pse_status       ON photo_stock_entries (status);
CREATE INDEX IF NOT EXISTS idx_pse_created_at   ON photo_stock_entries (created_at);

CREATE TABLE IF NOT EXISTS photo_stock_entry_items (
    id                     BIGSERIAL PRIMARY KEY,
    photo_stock_entry_id   BIGINT          NOT NULL REFERENCES photo_stock_entries(id),
    item_name              VARCHAR(255)    NOT NULL,
    quantity               NUMERIC(15, 3),
    unit                   VARCHAR(50),
    unit_price             NUMERIC(15, 2),
    category               VARCHAR(100)    DEFAULT 'General',
    confidence_score       NUMERIC(5, 2),
    validation_errors      TEXT,
    created_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_psei_entry_id ON photo_stock_entry_items (photo_stock_entry_id);
