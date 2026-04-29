package com.example.massivo.system.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MonthlySummaryDTO(UUID financialAccountId, LocalDate month, BigDecimal monthlyBalance, long totalTransactions) {}
