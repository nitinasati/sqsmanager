package com.glic.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
@TestPropertySource(properties = {
    "aws.sqs.url=https://sqs.test.amazonaws.com/123456789012/test-queue",
    "aws.sqs.queue.name=test-queue"
})
public class TestConfig {

    @Bean
    @Primary
    public SqsClient sqsClient() {
        return mock(SqsClient.class);
    }
} 