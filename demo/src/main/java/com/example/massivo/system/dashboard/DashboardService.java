package com.example.massivo.system.dashboard;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AccountBalanceDTO> getBalances(UUID companyId) {
        return jdbc.query("""
                SELECT fa.id, fa.name, fa.account_number, fa.balance, fa.balance_updated_at
                FROM financial_accounts fa
                WHERE fa.company_id = ? AND fa.active = true
                ORDER BY fa.name
                """, (rs, i) -> new AccountBalanceDTO(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("account_number"),
                        rs.getBigDecimal("balance"),
                        rs.getTimestamp("balance_updated_at") != null ? rs.getTimestamp("balance_updated_at").toInstant() : null
                ), companyId);
    }

    public List<MonthlySummaryDTO> getMonthlySummary(UUID companyId, int year) {
        return jdbc.query("""
                SELECT financial_account_id, month, monthly_balance, total_transactions
                FROM mv_balance_by_account
                WHERE company_id = ? AND EXTRACT(YEAR FROM month) = ?
                ORDER BY month, financial_account_id
                """, (rs, i) -> new MonthlySummaryDTO(
                        UUID.fromString(rs.getString("financial_account_id")),
                        rs.getDate("month").toLocalDate(),
                        rs.getBigDecimal("monthly_balance"),
                        rs.getLong("total_transactions")
                ), companyId, year);
    }
}
