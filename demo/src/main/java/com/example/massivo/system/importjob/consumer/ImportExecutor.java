package com.example.massivo.system.importjob.consumer;

import com.example.massivo.common.aws.S3Service;
import com.example.massivo.system.importjob.ImportJob;
import com.example.massivo.system.importjob.enums.ImportStatus;
import com.example.massivo.system.importjob.processor.BatchValidationService;
import com.example.massivo.system.importjob.processor.ChunkProcessor;
import com.example.massivo.system.importjob.processor.PromotionService;
import com.example.massivo.system.importjob.util.CsvParserService;
import com.example.massivo.system.importjob.util.CsvRow;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImportExecutor {

    private static final Logger log = LoggerFactory.getLogger(ImportExecutor.class);

    private final ImportJobUpdater updater;
    private final S3Service s3Service;
    private final CsvParserService csvParser;
    private final ChunkProcessor chunkProcessor;
    private final BatchValidationService batchValidation;
    private final PromotionService promotionService;

    @Value("${app.import.chunk-size:10000}") private int chunkSize;
    @Value("${app.import.error-threshold:0.10}") private double errorThreshold;

    public ImportExecutor(ImportJobUpdater updater, S3Service s3Service, CsvParserService csvParser,
                          ChunkProcessor chunkProcessor, BatchValidationService batchValidation,
                          PromotionService promotionService) {
        this.updater = updater;
        this.s3Service = s3Service;
        this.csvParser = csvParser;
        this.chunkProcessor = chunkProcessor;
        this.batchValidation = batchValidation;
        this.promotionService = promotionService;
    }

    public void execute(UUID importId) {
        ImportJob job = updater.claimJob(importId);
        if (job == null) {
            log.info("Import {} not found or not pending", importId);
            return;
        }

        try {
            processFile(job);
        } catch (Exception e) {
            log.error("Import {} failed: {}", importId, e.getMessage(), e);
            updater.updateStatus(importId, 0, 0, ImportStatus.FAILED);
        }
    }

    private void processFile(ImportJob job) throws Exception {
        InputStream input = s3Service.download(job.getStoragePath());
        int totalOk = 0, totalError = 0, rowNum = 0;

        try (CSVReader reader = csvParser.createReader(input)) {
            csvParser.validateHeader(reader.readNext());

            List<CsvRow> chunk = new ArrayList<>(chunkSize);
            String[] line;
            while ((line = reader.readNext()) != null) {
                rowNum++;
                chunk.add(csvParser.parseRow(line, rowNum));

                if (chunk.size() >= chunkSize) {
                    int[] result = chunkProcessor.process(chunk, job.getId(), job.getCompanyId());
                    totalOk += result[0];
                    totalError += result[1];
                    updater.updateProgress(job.getId(), rowNum, totalError);
                    chunk.clear();
                }
            }
            if (!chunk.isEmpty()) {
                int[] result = chunkProcessor.process(chunk, job.getId(), job.getCompanyId());
                totalOk += result[0];
                totalError += result[1];
            }
        }

        updater.updateStatus(job.getId(), rowNum, totalError, ImportStatus.VALIDATING);

        int refErrors = batchValidation.validate(job.getId(), job.getCompanyId());
        totalError += refErrors;

        if (rowNum > 0 && (double) totalError / rowNum > errorThreshold) {
            updater.updateStatus(job.getId(), rowNum, totalError, ImportStatus.FAILED);
            log.warn("Import {} failed: error rate {}/{}", job.getId(), totalError, rowNum);
            return;
        }

        int promoted = promotionService.promote(job.getId());
        log.info("Import {} promoted {} rows", job.getId(), promoted);

        updater.updateStatus(job.getId(), rowNum, totalError, ImportStatus.COMPLETED);
    }
}
