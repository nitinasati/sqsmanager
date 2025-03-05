package com.glic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glic.service.RestApiService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.sqs.SqsClient;

@TestConfiguration
public class TestConfig {

    @Bean(name = "testRestTemplate")
    @Primary
    public RestTemplate testRestTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "testObjectMapper")
    @Primary
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper();
    }

    @Bean(name = "testRestApiService")
    @Primary
    public RestApiService testRestApiService(RestTemplate testRestTemplate, ObjectMapper testObjectMapper) {
        return new RestApiService(testRestTemplate, testObjectMapper, "http://localhost:8080");
    }

    @Bean(name = "testSqsClient")
    @Primary
    public SqsClient testSqsClient() {
        return SqsClient.builder().build();
    }
} 