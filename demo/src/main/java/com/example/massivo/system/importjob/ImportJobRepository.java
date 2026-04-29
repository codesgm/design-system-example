package com.example.massivo.system.importjob;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.massivo.system.importjob.enums.ImportStatus;
import java.util.Optional;
import java.util.UUID;

public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {

    Optional<ImportJob> findByCompanyIdAndFileHash(UUID companyId, String fileHash);

    Page<ImportJob> findByCompanyId(UUID companyId, Pageable pageable);

    Page<ImportJob> findByCompanyIdAndStatus(UUID companyId, ImportStatus status, Pageable pageable);

    @Query(value = "SELECT * FROM imports WHERE status = 'PENDING' ORDER BY created_at LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<ImportJob> findPendingForProcessing();
}
