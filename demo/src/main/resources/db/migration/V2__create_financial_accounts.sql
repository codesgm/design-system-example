CREATE TABLE financial_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id),
    bank_code VARCHAR(10),
    agency VARCHAR(20),
    account_number VARCHAR(30) NOT NULL,
    name VARCHAR(255),
    balance NUMERIC(18,4) NOT NULL DEFAULT 0,
    balance_updated_at TIMESTAMPTZ,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (company_id, account_number)
);
