package com.example.massivo.system.importjob.consumer;

import com.example.massivo.system.importjob.ImportJob;
import com.example.massivo.system.importjob.ImportJobRepository;
import com.example.massivo.system.importjob.enums.ImportStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ImportJobUpdater {

    private final ImportJobRepository repo;

    public ImportJobUpdater(ImportJobRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportJob claimJob(UUID importId) {
        ImportJob job = repo.findById(importId).orElse(null);
        if (job == null || job.getStatus() != ImportStatus.PENDING) return null;
        job.setStatus(ImportStatus.PROCESSING);
        job.setWorkerId("worker-" + ProcessHandle.current().pid());
        job.setStartedAt(Instant.now());
        return repo.saveAndFlush(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(UUID importId, int processedRows, int errorRows) {
        repo.findById(importId).ifPresent(job -> {
            job.setProcessedRows(processedRows);
            job.setErrorRows(errorRows);
            repo.saveAndFlush(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(UUID importId, int totalRows, int errorRows, ImportStatus status) {
        repo.findById(importId).ifPresent(job -> {
            job.setTotalRows(totalRows);
            job.setProcessedRows(totalRows);
            job.setErrorRows(errorRows);
            job.setStatus(status);
            if (status == ImportStatus.FAILED || status == ImportStatus.COMPLETED) job.setFinishedAt(Instant.now());
            repo.saveAndFlush(job);
        });
    }
}
