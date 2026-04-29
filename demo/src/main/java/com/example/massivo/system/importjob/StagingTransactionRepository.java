package com.example.massivo.system.importjob;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface StagingTransactionRepository extends JpaRepository<StagingTransaction, UUID> {

    @Modifying
    @Query("DELETE FROM StagingTransaction s WHERE s.importId = :importId")
    int deleteByImportId(UUID importId);

    long countByImportId(UUID importId);
}
