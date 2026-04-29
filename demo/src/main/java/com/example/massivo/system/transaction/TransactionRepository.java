package com.example.massivo.system.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.companyId = :companyId" +
            " AND (:accountId IS NULL OR t.financialAccountId = :accountId)" +
            " AND (:from IS NULL OR t.dataLancamento >= :from)" +
            " AND (:to IS NULL OR t.dataLancamento <= :to)" +
            " AND (:nsu IS NULL OR t.nsu = :nsu)" +
            " AND (:tipo IS NULL OR t.tipo = :tipo)" +
            " AND (:search IS NULL OR LOWER(t.descricao) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Transaction> findFiltered(UUID companyId, UUID accountId, LocalDate from, LocalDate to,
                                   String nsu, String tipo, String search, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.importId = :importId")
    int deleteByImportId(UUID importId);
}
