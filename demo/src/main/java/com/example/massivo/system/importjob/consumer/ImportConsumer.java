package com.example.massivo.system.importjob.consumer;

import com.example.massivo.common.aws.SQSService;
import com.example.massivo.system.importjob.dto.ImportSqsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ImportConsumer {

    private static final Logger log = LoggerFactory.getLogger(ImportConsumer.class);

    private final SQSService sqsService;
    private final ImportExecutor importExecutor;
    private final ExecutorService workers = Executors.newFixedThreadPool(3);

    @Value("${aws.sqs.import-queue-url}")
    private String importQueueUrl;

    public ImportConsumer(SQSService sqsService, ImportExecutor importExecutor) {
        this.sqsService = sqsService;
        this.importExecutor = importExecutor;
    }

    @Scheduled(fixedDelay = 2000)
    public void pollMessages() {
        try {
            sqsService.processAndDeleteMessages(importQueueUrl, ImportSqsMessage.class, msg -> {
                log.info("Dispatching import: {}", msg.importId());
                workers.submit(() -> {
                    try {
                        importExecutor.execute(msg.importId());
                    } catch (Exception e) {
                        log.error("Worker error for import {}: {}", msg.importId(), e.getMessage());
                    }
                });
            });
        } catch (Exception e) {
            log.error("Error polling SQS: {}", e.getMessage());
        }
    }
}
