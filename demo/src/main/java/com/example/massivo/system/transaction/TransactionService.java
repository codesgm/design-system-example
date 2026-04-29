package com.example.massivo.system.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final JdbcTemplate jdbc;

    public TransactionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<Transaction> findFiltered(UUID companyId, UUID accountId, LocalDate from, LocalDate to,
                                          String nsu, String tipo, String search, Pageable pageable) {
        StringBuilder where = new StringBuilder("WHERE t.company_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(companyId);

        if (accountId != null) { where.append(" AND t.financial_account_id = ?"); params.add(accountId); }
        if (from != null) { where.append(" AND t.data_lancamento >= ?"); params.add(from); }
        if (to != null) { where.append(" AND t.data_lancamento <= ?"); params.add(to); }
        if (nsu != null && !nsu.isBlank()) { where.append(" AND t.nsu = ?"); params.add(nsu); }
        if (tipo != null && !tipo.isBlank()) { where.append(" AND t.tipo = ?"); params.add(tipo); }
        if (search != null && !search.isBlank()) { where.append(" AND t.descricao ILIKE ?"); params.add("%" + search + "%"); }

        String countSql = "SELECT count(*) FROM transactions t " + where;
        long total = jdbc.queryForObject(countSql, Long.class, params.toArray());

        String dataSql = "SELECT * FROM transactions t " + where + " ORDER BY t.data_lancamento DESC LIMIT ? OFFSET ?";
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Transaction> rows = jdbc.query(dataSql, (rs, i) -> {
            Transaction t = new Transaction();
            t.setId(UUID.fromString(rs.getString("id")));
            t.setCompanyId(UUID.fromString(rs.getString("company_id")));
            t.setFinancialAccountId(UUID.fromString(rs.getString("financial_account_id")));
            t.setImportId(UUID.fromString(rs.getString("import_id")));
            t.setNsu(rs.getString("nsu"));
            t.setDescricao(rs.getString("descricao"));
            t.setContaBancaria(rs.getString("conta_bancaria"));
            t.setDataCompetencia(rs.getDate("data_competencia") != null ? rs.getDate("data_competencia").toLocalDate() : null);
            t.setDataVencimento(rs.getDate("data_vencimento") != null ? rs.getDate("data_vencimento").toLocalDate() : null);
            t.setDataLancamento(rs.getDate("data_lancamento").toLocalDate());
            t.setValorTotal(rs.getBigDecimal("valor_total"));
            t.setTipo(rs.getString("tipo"));
            return t;
        }, params.toArray());

        return new PageImpl<>(rows, pageable, total);
    }
}
