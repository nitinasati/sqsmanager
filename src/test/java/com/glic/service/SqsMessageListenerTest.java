package com.glic.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqsMessageListenerTest {

    @Mock
    private SqsClient sqsClient;

    private SqsMessageListener messageListener;
    private static final String QUEUE_URL = "https://sqs.test.amazonaws.com/123456789012/test-queue";

    @BeforeEach
    void setUp() {
        messageListener = new SqsMessageListener(sqsClient);
        ReflectionTestUtils.setField(messageListener, "queueUrl", QUEUE_URL);
    }

    @Test
    void testProcessMessage() {
        // Arrange
        Message message = Message.builder()
                .messageId("test-message-id")
                .body("test-message-body")
                .receiptHandle("test-receipt-handle")
                .build();

        // Act
        messageListener.processMessage(message);

        // Assert
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testDeleteMessage() {
        // Arrange
        String receiptHandle = "test-receipt-handle";

        // Act
        messageListener.deleteMessage(receiptHandle);

        // Assert
        DeleteMessageRequest expectedRequest = DeleteMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .receiptHandle(receiptHandle)
                .build();
        verify(sqsClient).deleteMessage(expectedRequest);
    }

    @Test
    void testMonitorQueue() throws InterruptedException {
        // Arrange
        Message message1 = Message.builder()
                .messageId("message-1")
                .body("test-message-1")
                .receiptHandle("receipt-1")
                .build();

        Message message2 = Message.builder()
                .messageId("message-2")
                .body("test-message-2")
                .receiptHandle("receipt-2")
                .build();

        List<Message> messages = Arrays.asList(message1, message2);

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(messages)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response);

        // Act
        messageListener.startMonitoring();
        Thread.sleep(1000); // Give some time for processing
        messageListener.stopMonitoring();

        // Assert
        verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testMonitorQueueWithException() throws InterruptedException {
        // Arrange
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        messageListener.startMonitoring();
        Thread.sleep(1000); // Give some time for processing
        messageListener.stopMonitoring();

        // Assert
        verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testMonitorQueueWithEmptyResponse() throws InterruptedException {
        // Arrange
        ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
                .messages(List.of())
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(emptyResponse);

        // Act
        messageListener.startMonitoring();
        Thread.sleep(1000); // Give some time for processing
        messageListener.stopMonitoring();

        // Assert
        verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testStartMonitoringMultipleTimes() {
        // Act
        messageListener.startMonitoring();
        messageListener.startMonitoring();

        // Assert
        verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
    }
} 