package com.example.massivo.system.importjob.processor;

import com.example.massivo.system.importjob.util.CsvRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChunkProcessor {

    private final JdbcTemplate jdbc;

    public ChunkProcessor(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int[] process(List<CsvRow> chunk, UUID importId, UUID companyId) {
        List<CsvRow> valid = chunk.stream().filter(CsvRow::valid).toList();
        List<CsvRow> invalid = chunk.stream().filter(r -> !r.valid()).toList();

        if (!valid.isEmpty()) insertStaging(valid, importId, companyId);
        if (!invalid.isEmpty()) insertErrors(invalid, importId);

        return new int[]{valid.size(), invalid.size()};
    }

    private void insertStaging(List<CsvRow> rows, UUID importId, UUID companyId) {
        String sql = """
                INSERT INTO staging_transactions (id, company_id, import_id, nsu, descricao, conta_bancaria,
                    data_competencia, data_vencimento, data_lancamento, valor_total, tipo, created_at)
                VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (company_id, nsu) DO NOTHING
                """;
        jdbc.batchUpdate(sql, rows, rows.size(), (ps, row) -> {
            ps.setObject(1, companyId);
            ps.setObject(2, importId);
            ps.setString(3, row.nsu());
            ps.setString(4, row.descricao());
            ps.setString(5, row.contaBancaria());
            ps.setObject(6, row.dataCompetencia());
            ps.setObject(7, row.dataVencimento());
            ps.setObject(8, row.dataLancamento());
            ps.setBigDecimal(9, row.valorTotal());
            ps.setString(10, row.tipo());
            ps.setTimestamp(11, Timestamp.from(Instant.now()));
        });
    }

    private void insertErrors(List<CsvRow> rows, UUID importId) {
        String sql = "INSERT INTO import_errors (import_id, row_number, row_content, error_type, error_message, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbc.batchUpdate(sql, rows, rows.size(), (ps, row) -> {
            ps.setObject(1, importId);
            ps.setInt(2, row.rowNumber());
            ps.setString(3, row.rawContent());
            ps.setString(4, row.errorType());
            ps.setString(5, row.errorMessage());
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
        });
    }
}
