package com.example.massivo.system.importjob;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
    Page<ImportError> findByImportId(UUID importId, Pageable pageable);
    long countByImportId(UUID importId);
}
