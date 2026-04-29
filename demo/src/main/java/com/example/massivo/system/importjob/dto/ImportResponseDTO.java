package com.example.massivo.system.importjob.dto;

import com.example.massivo.system.importjob.ImportJob;
import com.example.massivo.system.importjob.enums.ImportStatus;
import java.time.Instant;
import java.util.UUID;

public record ImportResponseDTO(
        UUID id, UUID companyId, String fileName, ImportStatus status,
        int totalRows, int processedRows, int errorRows,
        Instant startedAt, Instant finishedAt, Instant createdAt
) {
    public static ImportResponseDTO from(ImportJob job) {
        return new ImportResponseDTO(
                job.getId(), job.getCompanyId(), job.getFileName(), job.getStatus(),
                job.getTotalRows(), job.getProcessedRows(), job.getErrorRows(),
                job.getStartedAt(), job.getFinishedAt(), job.getCreatedAt()
        );
    }
}
