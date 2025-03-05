package com.glic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glic.service.RestApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Configuration class for AWS and application components.
 * Provides beans for AWS SQS client, ObjectMapper, and REST API service.
 * This class handles the configuration of AWS SQS client and various application services.
 */
@Configuration
public class Config {

    /**
     * Default constructor for Config class.
     * Required by Spring for configuration class instantiation.
     */
    public Config() {
    }

    /**
     * AWS region where the SQS queue is located.
     */
    @Value("${aws.region}")
    private String awsRegion;

    /**
     * The ARN (Amazon Resource Name) of the SQS queue.
     */
    @Value("${aws.sqs.arn}")
    private String sqsQueueArn;
    
    /**
     * The URL of the SQS queue.
     */
    @Value("${aws.sqs.url}")
    private String sqsQueueUrl;

    /**
     * The base URL of the REST API service.
     */
    @Value("${api.base.url}")
    private String restApiBaseUrl;

    /**
     * Creates and configures a RestTemplate bean for making HTTP requests.
     *
     * @return A configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates an AWS SQS client bean configured with the default credentials provider.
     * The credentials will be loaded from the default credential provider chain,
     * which includes environment variables, Java system properties, AWS credentials file,
     * and Amazon ECS container credentials.
     *
     * @return a configured SqsClient instance
     */
    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Creates an ObjectMapper bean for JSON serialization/deserialization.
     *
     * @return a configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Creates a RestApiService bean configured with the provided base URL.
     *
     * @param restTemplate the RestTemplate instance for making HTTP requests
     * @param objectMapper the ObjectMapper instance for JSON serialization/deserialization
     * @return a configured RestApiService instance
     */
    @Bean
    public RestApiService restApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new RestApiService(restTemplate, objectMapper, restApiBaseUrl);
    }
}
