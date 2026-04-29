package com.example.massivo.system.financialaccount;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_accounts")
public class FinancialAccount {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "bank_code")
    private String bankCode;

    private String agency;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private String name;

    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "balance_updated_at")
    private Instant balanceUpdatedAt;

    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }
}
