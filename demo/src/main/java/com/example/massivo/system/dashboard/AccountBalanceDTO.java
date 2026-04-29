package com.example.massivo.system.dashboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountBalanceDTO(UUID accountId, String name, String accountNumber, BigDecimal balance, Instant balanceUpdatedAt) {}
