package com.example.massivo.system.importjob.processor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PromotionService {

    private final JdbcTemplate jdbc;

    public PromotionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int promote(UUID importId) {
        // INSERT INTO transactions SELECT FROM staging
        int promoted = jdbc.update("""
                INSERT INTO transactions (id, company_id, financial_account_id, import_id, nsu, descricao,
                    conta_bancaria, data_competencia, data_vencimento, data_lancamento, valor_total, tipo, created_at)
                SELECT id, company_id, financial_account_id, import_id, nsu, descricao,
                    conta_bancaria, data_competencia, data_vencimento, data_lancamento, valor_total, tipo, created_at
                FROM staging_transactions WHERE import_id = ?
                """, importId);

        // Atualiza saldos das contas afetadas
        jdbc.update("""
                UPDATE financial_accounts fa
                SET balance = (
                    SELECT COALESCE(SUM(t.valor_total), 0)
                    FROM transactions t
                    WHERE t.financial_account_id = fa.id AND t.company_id = fa.company_id
                ), balance_updated_at = now(), updated_at = now()
                WHERE fa.id IN (
                    SELECT DISTINCT s.financial_account_id FROM staging_transactions s WHERE s.import_id = ?
                )
                """, importId);

        // Refresh materialized view
        jdbc.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_balance_by_account");

        // Limpa staging
        jdbc.update("DELETE FROM staging_transactions WHERE import_id = ?", importId);

        return promoted;
    }
}
