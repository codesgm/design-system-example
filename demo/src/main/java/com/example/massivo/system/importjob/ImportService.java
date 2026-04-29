package com.example.massivo.system.importjob;

import com.example.massivo.common.aws.S3Service;
import com.example.massivo.common.aws.SQSService;
import com.example.massivo.common.exception.ConflictException;
import com.example.massivo.common.exception.NotFoundException;
import com.example.massivo.system.importjob.dto.ImportResponseDTO;
import com.example.massivo.system.importjob.dto.ImportSqsMessage;
import com.example.massivo.system.importjob.enums.ImportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final ImportJobRepository importJobRepository;
    private final ImportErrorRepository importErrorRepository;
    private final S3Service s3Service;
    private final SQSService sqsService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Value("${aws.sqs.import-queue-url}")
    private String importQueueUrl;

    public ImportService(ImportJobRepository importJobRepository, ImportErrorRepository importErrorRepository,
                         S3Service s3Service, SQSService sqsService, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.importJobRepository = importJobRepository;
        this.importErrorRepository = importErrorRepository;
        this.s3Service = s3Service;
        this.sqsService = sqsService;
        this.jdbc = jdbc;
    }

    public ImportResponseDTO createImport(UUID companyId, MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String fileHash = sha256(new java.io.ByteArrayInputStream(bytes));

            importJobRepository.findByCompanyIdAndFileHash(companyId, fileHash)
                    .ifPresent(existing -> { throw new ConflictException("Arquivo já importado anteriormente"); });

            // Conta linhas (exclui header)
            int lineCount = 0;
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.ByteArrayInputStream(bytes)))) {
                reader.readLine(); // skip header
                while (reader.readLine() != null) lineCount++;
            }

            ImportJob job = new ImportJob(companyId, fileHash, file.getOriginalFilename(), file.getSize(), "");
            job.setTotalRows(lineCount);
            job = importJobRepository.save(job);

            String s3Key = s3Service.buildKey(companyId, job.getId(), file.getOriginalFilename());
            s3Service.upload(s3Key, new java.io.ByteArrayInputStream(bytes), bytes.length);
            job.setStoragePath(s3Key);
            importJobRepository.save(job);

            sqsService.sendMessage(importQueueUrl, new ImportSqsMessage(job.getId()));
            log.info("Import created: id={}, file={}, lines={}", job.getId(), file.getOriginalFilename(), lineCount);

            return ImportResponseDTO.from(job);
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar importação", e);
        }
    }

    public Page<ImportResponseDTO> findByCompanyId(UUID companyId, ImportStatus status, Pageable pageable) {
        Page<ImportJob> page = (status != null)
                ? importJobRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                : importJobRepository.findByCompanyId(companyId, pageable);
        return page.map(ImportResponseDTO::from);
    }

    public ImportResponseDTO findById(UUID id) {
        return importJobRepository.findById(id)
                .map(ImportResponseDTO::from)
                .orElseThrow(() -> new NotFoundException("Importação não encontrada"));
    }

    public Page<ImportError> getErrors(UUID importId, Pageable pageable) {
        return importErrorRepository.findByImportId(importId, pageable);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resetAll(UUID companyId) {
        jdbc.update("DELETE FROM import_errors WHERE import_id IN (SELECT id FROM imports WHERE company_id = ?)", companyId);
        jdbc.update("DELETE FROM transactions WHERE company_id = ?", companyId);
        jdbc.update("DELETE FROM staging_transactions WHERE company_id = ?", companyId);
        jdbc.update("DELETE FROM imports WHERE company_id = ?", companyId);
        jdbc.update("UPDATE financial_accounts SET balance = 0, balance_updated_at = null WHERE company_id = ?", companyId);
        jdbc.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_balance_by_account");
        jdbc.update("DELETE FROM shedlock WHERE name = 'import_consumer'");
        sqsService.purgeQueue(importQueueUrl);
    }

    private String sha256(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
