CREATE TABLE transactions (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    financial_account_id UUID NOT NULL,
    import_id UUID NOT NULL,
    nsu VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    conta_bancaria VARCHAR(30) NOT NULL,
    data_competencia DATE,
    data_vencimento DATE,
    data_lancamento DATE NOT NULL,
    valor_total NUMERIC(18,4) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id),
    UNIQUE (company_id, nsu),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (financial_account_id) REFERENCES financial_accounts(id),
    FOREIGN KEY (import_id) REFERENCES imports(id)
);

CREATE INDEX idx_transactions_company_date ON transactions(company_id, data_lancamento);
