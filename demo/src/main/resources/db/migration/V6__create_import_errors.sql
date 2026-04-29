CREATE TABLE import_errors (
    id BIGSERIAL PRIMARY KEY,
    import_id UUID NOT NULL REFERENCES imports(id),
    row_number INT NOT NULL,
    row_content TEXT,
    error_type VARCHAR(50) NOT NULL,
    error_message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_import_errors_import_id ON import_errors(import_id);
