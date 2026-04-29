CREATE TABLE staging_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    import_id UUID NOT NULL,
    financial_account_id UUID,
    nsu VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    conta_bancaria VARCHAR(30) NOT NULL,
    data_competencia DATE,
    data_vencimento DATE,
    data_lancamento DATE NOT NULL,
    valor_total NUMERIC(18,4) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (company_id, nsu)
);

CREATE INDEX idx_staging_import_id ON staging_transactions(import_id);
