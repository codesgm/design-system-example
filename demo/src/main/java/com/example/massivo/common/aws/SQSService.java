package com.example.massivo.common.aws;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class SQSService extends AWSConfig {

    private static final Logger log = LoggerFactory.getLogger(SQSService.class);
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.endpoint}") private String sqsEndpoint;

    public SQSService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private AmazonSQS getClient() {
        AmazonSQSClientBuilder builder = AmazonSQSClientBuilder.standard()
                .withCredentials(getCredentials());
        if (isLocal()) {
            builder.withEndpointConfiguration(new EndpointConfiguration(sqsEndpoint, region));
        } else {
            builder.withRegion(region);
        }
        return builder.build();
    }

    public <T> void sendMessage(String queueUrl, T message) {
        try {
            String body = objectMapper.writeValueAsString(message);
            getClient().sendMessage(new SendMessageRequest()
                    .withQueueUrl(queueUrl).withMessageBody(body).withDelaySeconds(5));
            log.info("SQS message sent to {}", queueUrl);
        } catch (Exception e) {
            log.error("Error sending SQS message: {}", e.getMessage());
        }
    }

    public void purgeQueue(String queueUrl) {
        try {
            getClient().purgeQueue(new com.amazonaws.services.sqs.model.PurgeQueueRequest().withQueueUrl(queueUrl));
            log.info("SQS queue purged: {}", queueUrl);
        } catch (Exception e) {
            log.warn("Error purging SQS queue: {}", e.getMessage());
        }
    }

    public <T> void processAndDeleteMessages(String queueUrl, Class<T> clazz, Consumer<T> consumer) {
        try {
            List<Message> messages = getClient().receiveMessage(
                    new ReceiveMessageRequest()
                            .withQueueUrl(queueUrl)
                            .withMaxNumberOfMessages(3)
                            .withWaitTimeSeconds(2)
            ).getMessages();

            for (Message msg : messages) {
                try {
                    T parsed = objectMapper.readValue(msg.getBody(), clazz);
                    consumer.accept(parsed);
                    getClient().deleteMessage(new DeleteMessageRequest()
                            .withQueueUrl(queueUrl).withReceiptHandle(msg.getReceiptHandle()));
                    log.info("SQS message processed and deleted");
                } catch (Exception e) {
                    log.error("Error processing SQS message: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error receiving from SQS: {}", e.getMessage());
        }
    }
}
