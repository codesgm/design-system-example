CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_transactions_company_account ON transactions(company_id, financial_account_id);
CREATE INDEX idx_transactions_nsu ON transactions(company_id, nsu);
CREATE INDEX idx_transactions_tipo ON transactions(company_id, tipo);
CREATE INDEX idx_transactions_descricao_trgm ON transactions USING gin (descricao gin_trgm_ops);
