package com.example.massivo.system.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "financial_account_id", nullable = false)
    private UUID financialAccountId;

    @Column(name = "import_id", nullable = false)
    private UUID importId;

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
    public UUID getFinancialAccountId() { return financialAccountId; }
    public UUID getImportId() { return importId; }
    public String getNsu() { return nsu; }
    public String getDescricao() { return descricao; }
    public String getContaBancaria() { return contaBancaria; }
    public LocalDate getDataCompetencia() { return dataCompetencia; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public LocalDate getDataLancamento() { return dataLancamento; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getTipo() { return tipo; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(UUID id) { this.id = id; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public void setFinancialAccountId(UUID financialAccountId) { this.financialAccountId = financialAccountId; }
    public void setImportId(UUID importId) { this.importId = importId; }
    public void setNsu(String nsu) { this.nsu = nsu; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setContaBancaria(String contaBancaria) { this.contaBancaria = contaBancaria; }
    public void setDataCompetencia(LocalDate dataCompetencia) { this.dataCompetencia = dataCompetencia; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public void setDataLancamento(LocalDate dataLancamento) { this.dataLancamento = dataLancamento; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
