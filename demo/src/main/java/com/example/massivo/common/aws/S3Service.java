package com.example.massivo.common.aws;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service extends AWSConfig {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket}") private String bucket;
    @Value("${aws.s3.endpoint}") private String endpoint;

    private AmazonS3 client;

    @PostConstruct
    public void init() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(getCredentials())
                .withClientConfiguration(getClientConfiguration());
        if (isLocal()) {
            builder.withEndpointConfiguration(new EndpointConfiguration(endpoint, region))
                    .withPathStyleAccessEnabled(true);
        } else {
            builder.withRegion(region);
        }
        this.client = builder.build();
    }

    public String buildKey(UUID companyId, UUID importId, String fileName) {
        return "imports/" + companyId + "/" + importId + "/" + fileName;
    }

    public void upload(String key, InputStream input, long size) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(size);
        client.putObject(new PutObjectRequest(bucket, key, input, meta));
        log.info("Uploaded to S3: {}", key);
    }

    public InputStream download(String key) {
        return client.getObject(new GetObjectRequest(bucket, key)).getObjectContent();
    }
}
