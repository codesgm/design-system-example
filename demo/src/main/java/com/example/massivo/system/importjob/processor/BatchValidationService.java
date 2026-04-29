package com.example.massivo.system.importjob.processor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class BatchValidationService {

    private final JdbcTemplate jdbc;

    public BatchValidationService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Resolve financial_account_id no staging e registra erros para contas inexistentes.
     * Retorna quantidade de linhas com erro referencial.
     */
    public int validate(UUID importId, UUID companyId) {
        // Atualiza financial_account_id para contas que existem
        jdbc.update("""
                UPDATE staging_transactions s
                SET financial_account_id = fa.id
                FROM financial_accounts fa
                WHERE fa.company_id = s.company_id
                  AND fa.account_number = s.conta_bancaria
                  AND s.import_id = ?
                  AND s.financial_account_id IS NULL
                """, importId);

        // Registra erros para contas inexistentes
        int errors = jdbc.update("""
                INSERT INTO import_errors (import_id, row_number, row_content, error_type, error_message, created_at)
                SELECT s.import_id, 0, s.nsu || ',' || s.conta_bancaria, 'REFERENCE',
                       'Conta bancária não encontrada: ' || s.conta_bancaria, ?
                FROM staging_transactions s
                WHERE s.import_id = ? AND s.financial_account_id IS NULL
                """, Timestamp.from(Instant.now()), importId);

        // Remove do staging as linhas sem conta
        jdbc.update("DELETE FROM staging_transactions WHERE import_id = ? AND financial_account_id IS NULL", importId);

        return errors;
    }
}
