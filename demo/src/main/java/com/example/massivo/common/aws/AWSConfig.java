package com.example.massivo.common.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import org.springframework.beans.factory.annotation.Value;

public abstract class AWSConfig {

    @Value("${aws.region}") protected String region;
    @Value("${aws.access-key}") protected String accessKey;
    @Value("${aws.secret-key}") protected String secretKey;
    @Value("${spring.profiles.active:local}") protected String activeProfile;

    protected boolean isLocal() { return "local".equals(activeProfile); }

    protected AWSCredentialsProvider getCredentials() {
        if (isLocal()) {
            return new AWSStaticCredentialsProvider(new BasicSessionCredentials(accessKey, secretKey, "token"));
        }
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }

    protected ClientConfiguration getClientConfiguration() {
        ClientConfiguration config = new ClientConfiguration();
        config.setConnectionTimeout(20_000);
        config.setSocketTimeout(20_000);
        return config;
    }
}
