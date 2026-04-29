CREATE TYPE import_status AS ENUM ('PENDING', 'PROCESSING', 'VALIDATING', 'COMPLETED', 'FAILED', 'REVERTED');

CREATE TABLE imports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id),
    file_hash VARCHAR(64) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    status import_status NOT NULL DEFAULT 'PENDING',
    total_rows INT DEFAULT 0,
    processed_rows INT DEFAULT 0,
    error_rows INT DEFAULT 0,
    worker_id VARCHAR(100),
    version INT NOT NULL DEFAULT 0,
    storage_path VARCHAR(1000) NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (company_id, file_hash)
);
