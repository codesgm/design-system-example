CREATE MATERIALIZED VIEW mv_balance_by_account AS
SELECT
    t.company_id,
    t.financial_account_id,
    date_trunc('month', t.data_lancamento) AS month,
    SUM(t.valor_total) AS monthly_balance,
    COUNT(*) AS total_transactions
FROM transactions t
GROUP BY t.company_id, t.financial_account_id, date_trunc('month', t.data_lancamento);

CREATE UNIQUE INDEX idx_mv_balance ON mv_balance_by_account(company_id, financial_account_id, month);
