package com.example.massivo.system.importjob;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staging_transactions")
public class StagingTransaction {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "import_id", nullable = false)
    private UUID importId;

    @Column(name = "financial_account_id")
    private UUID financialAccountId;

    @Column(nullable = false)
    private String nsu;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "conta_bancaria", nullable = false)
    private String contaBancaria;

    @Column(name = "data_competencia")
    private LocalDate dataCompetencia;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "valor_total", precision = 18, scale = 4, nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getImportId() { return importId; }
    public UUID getFinancialAccountId() { return financialAccountId; }
    public String getNsu() { return nsu; }
    public String getContaBancaria() { return contaBancaria; }
    public LocalDate getDataLancamento() { return dataLancamento; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getTipo() { return tipo; }

    public void setFinancialAccountId(UUID financialAccountId) { this.financialAccountId = financialAccountId; }
}
