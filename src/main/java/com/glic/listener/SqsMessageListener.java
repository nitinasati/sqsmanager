package com.glic.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glic.model.Product;
import com.glic.service.RestApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listener class for processing messages from an AWS SQS queue.
 * Continuously polls the queue for messages and processes them by creating
 * products through a REST API call.
 */
@Slf4j
@Component
public class SqsMessageListener {

    /**
     * The AWS SQS client for queue operations.
     */
    private final SqsClient sqsClient;

    /**
     * The ObjectMapper instance for JSON deserialization.
     */
    private final ObjectMapper objectMapper;

    /**
     * The service for making REST API calls.
     */
    private final RestApiService restApiService;

    /**
     * The URL of the SQS queue to poll from.
     */
    private final String queueUrl;

    /**
     * Flag indicating whether the message listener is running.
     */
    private final AtomicBoolean isRunning;

    /**
     * The executor service for running the message polling thread.
     */
    private final ExecutorService executorService;

    /**
     * Maximum number of retry attempts for failed operations.
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Delay in milliseconds between retry attempts.
     */
    private static final long RETRY_DELAY_MS = 5000;

    /**
     * Maximum number of messages to receive in a single poll.
     */
    private static final int MAX_MESSAGES = 10;

    /**
     * Time in seconds to wait for messages in long polling.
     */
    private static final int WAIT_TIME_SECONDS = 20;

    /**
     * Constructs a new SqsMessageListener with the required dependencies.
     *
     * @param sqsClient The AWS SQS client for queue operations
     * @param objectMapper The ObjectMapper for JSON deserialization
     * @param restApiService The service for making REST API calls
     * @param queueUrl The URL of the SQS queue to poll from
     */
    @Autowired
    public SqsMessageListener(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            RestApiService restApiService,
            @Value("${aws.sqs.url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.restApiService = restApiService;
        this.queueUrl = queueUrl;
        this.isRunning = new AtomicBoolean(false);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Starts the message listener and begins polling for messages.
     * This method is called to manually start the listener.
     */
    public void start() {
        log.info("Starting SQS message listener");
        isRunning.set(true);
        executorService.submit(this::pollMessages);
        log.info("SQS message listener started");
    }

    /**
     * Stops the message listener and stops polling for messages.
     * This method is called to manually stop the listener.
     */
    public void stop() {
        log.info("Stopping SQS message listener");
        isRunning.set(false);
        executorService.shutdown();
        log.info("SQS message listener stopped");
    }

    /**
     * Starts listening for messages from the SQS queue.
     * This method is an alias for start() to maintain compatibility.
     */
    public void startListening() {
        start();
    }

    /**
     * Initializes the message listener and starts polling for messages.
     * This method is called after the bean is constructed and all dependencies are injected.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing SQS message listener");
        isRunning.set(true);
        executorService.submit(this::pollMessages);
        log.info("SQS message listener initialized and started");
    }

    /**
     * Shuts down the message listener and stops polling for messages.
     * This method is called before the bean is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SQS message listener");
        isRunning.set(false);
        executorService.shutdown();
        log.info("SQS message listener shut down");
    }

    /**
     * Continuously polls the SQS queue for messages and processes them.
     * This method runs in a separate thread and handles message processing
     * with retry logic for error cases.
     */
    private void pollMessages() {
        while (isRunning.get()) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(MAX_MESSAGES)
                        .waitTimeSeconds(WAIT_TIME_SECONDS)
                        .build();

                log.debug("Polling SQS queue for messages");
                ReceiveMessageResponse response = sqsClient.receiveMessage(request);
                List<Message> messages = response.messages();

                if (messages.isEmpty()) {
                    log.debug("No messages received from SQS queue");
                    continue;
                }

                log.debug("Received {} messages from SQS queue", messages.size());
                processMessages(messages);
            } catch (Exception e) {
                log.error("Error polling SQS queue: {}", e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Polling interrupted: {}", ie.getMessage());
                }
            }
        }
    }

    /**
     * Processes a list of messages received from the SQS queue.
     * Each message is deserialized into a Product and created through the REST API.
     *
     * @param messages the list of messages to process
     */
    private void processMessages(List<Message> messages) {
        for (Message message : messages) {
            try {
                log.debug("Processing message: {}", message.body());
                Product product = objectMapper.readValue(message.body(), Product.class);
                restApiService.createProduct(product);
                log.debug("Successfully processed message and created product");
                
                // Delete the message after successful processing
                DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build();
                sqsClient.deleteMessage(deleteRequest);
                log.debug("Successfully deleted message from queue");
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage());
            }
        }
    }

    /**
     * Processes a single message received from the SQS queue.
     * This method is used for testing purposes and processes one message at a time.
     *
     * @param message the message to process
     */
    public void processMessage(Message message) {
        try {
            log.debug("Processing message: {}", message.body());
            Product product = objectMapper.readValue(message.body(), Product.class);
            restApiService.createProduct(product);
            log.debug("Successfully processed message and created product");
            
            // Delete the message after successful processing
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteRequest);
            log.debug("Successfully deleted message from queue");
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            throw new RuntimeException("Failed to process message", e);
        }
    }
} 