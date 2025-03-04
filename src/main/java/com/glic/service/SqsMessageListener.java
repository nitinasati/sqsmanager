package com.glic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SqsMessageListener {

    private final SqsClient sqsClient;
    private final ExecutorService executorService;
    private final AtomicBoolean isRunning;

    @Value("${aws.sqs.url}")
    private String queueUrl;

    public SqsMessageListener(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.executorService = Executors.newSingleThreadExecutor();
        this.isRunning = new AtomicBoolean(false);
    }

    @PostConstruct
    public void startMonitoring() {
        if (!isRunning.get()) {
            isRunning.set(true);
            executorService.submit(this::monitorQueue);
            log.info("SQS monitoring started");
        }
    }

    @PreDestroy
    public void stopMonitoring() {
        isRunning.set(false);
        executorService.shutdown();
        log.info("SQS monitoring stopped");
    }

    private void monitorQueue() {
        while (isRunning.get()) {
            try {
                ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(20)
                    .build();

                ReceiveMessageResponse response = sqsClient.receiveMessage(request);
                
                for (Message message : response.messages()) {
                    if (!isRunning.get()) break;
                    processMessage(message);
                    deleteMessage(message.receiptHandle());
                }
            } catch (Exception e) {
                log.error("Error processing messages: ", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    void processMessage(Message message) {
        log.info("Processing message: {}", message.body());
        // Add your message processing logic here
        // For example: parse JSON, save to database, trigger notifications, etc.
    }

    void deleteMessage(String receiptHandle) {
        DeleteMessageRequest request = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(receiptHandle)
            .build();
        
        sqsClient.deleteMessage(request);
        log.info("Message deleted successfully");
    }
} 