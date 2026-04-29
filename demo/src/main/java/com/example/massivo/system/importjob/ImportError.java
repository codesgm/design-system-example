package com.example.massivo.system.importjob;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "import_errors")
public class ImportError {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_id", nullable = false)
    private UUID importId;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "row_content")
    private String rowContent;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ImportError() {}

    public ImportError(UUID importId, int rowNumber, String rowContent, String errorType, String errorMessage) {
        this.importId = importId;
        this.rowNumber = rowNumber;
        this.rowContent = rowContent;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public UUID getImportId() { return importId; }
    public int getRowNumber() { return rowNumber; }
    public String getRowContent() { return rowContent; }
    public String getErrorType() { return errorType; }
    public String getErrorMessage() { return errorMessage; }
}
