package com.example.massivo.system.importjob;

import com.example.massivo.system.importjob.enums.ImportStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "imports")
public class ImportJob {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "file_hash", nullable = false)
    private String fileHash;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private ImportStatus status = ImportStatus.PENDING;

    @Column(name = "total_rows")
    private int totalRows;

    @Column(name = "processed_rows")
    private int processedRows;

    @Column(name = "error_rows")
    private int errorRows;

    @Column(name = "worker_id")
    private String workerId;

    @Version
    private int version;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ImportJob() {}

    public ImportJob(UUID companyId, String fileHash, String fileName, Long fileSizeBytes, String storagePath) {
        this.companyId = companyId;
        this.fileHash = fileHash;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.storagePath = storagePath;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getFileHash() { return fileHash; }
    public String getFileName() { return fileName; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public ImportStatus getStatus() { return status; }
    public int getTotalRows() { return totalRows; }
    public int getProcessedRows() { return processedRows; }
    public int getErrorRows() { return errorRows; }
    public String getStoragePath() { return storagePath; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public int getVersion() { return version; }

    public void setStatus(ImportStatus status) { this.status = status; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public void setProcessedRows(int processedRows) { this.processedRows = processedRows; }
    public void setErrorRows(int errorRows) { this.errorRows = errorRows; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
}
