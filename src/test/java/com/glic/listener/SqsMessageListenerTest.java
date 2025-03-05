package com.glic.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glic.model.Product;
import com.glic.service.RestApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqsMessageListenerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestApiService restApiService;

    private SqsMessageListener sqsMessageListener;
    private static final String QUEUE_URL = "https://sqs.test.amazonaws.com/123456789012/test-queue";
    private Thread pollingThread;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        sqsMessageListener = new SqsMessageListener(sqsClient, objectMapper, restApiService, QUEUE_URL);
        latch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() {
        sqsMessageListener.stop();
        if (pollingThread != null && pollingThread.isAlive()) {
            pollingThread.interrupt();
        }
    }

    @Test
    void processMessage_Success() throws Exception {
        // Arrange
        Message message = Message.builder()
                .body("{\"name\":\"Test Product\",\"price\":100.0,\"quantity\":10}")
                .receiptHandle("test-receipt-handle")
                .build();

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        when(objectMapper.readValue(message.body(), Product.class)).thenReturn(product);

        // Act
        sqsMessageListener.processMessage(message);

        // Assert
        verify(restApiService).createProduct(product);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void processMessage_WithInvalidJson() throws Exception {
        // Arrange
        String messageBody = "invalid-json";
        Message message = Message.builder()
                .messageId("test-message-id")
                .body(messageBody)
                .receiptHandle("test-receipt-handle")
                .build();

        RuntimeException expectedException = new RuntimeException("Invalid JSON");
        when(objectMapper.readValue(eq(messageBody), eq(Product.class)))
                .thenThrow(expectedException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> ReflectionTestUtils.invokeMethod(sqsMessageListener, "processMessage", message));
        assertEquals("Failed to process message", thrown.getMessage());
        verify(objectMapper).readValue(eq(messageBody), eq(Product.class));
        verify(restApiService, never()).createProduct(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void processMessage_WithRestApiError() throws Exception {
        // Arrange
        String messageBody = "{\"name\":\"Test Product\",\"price\":100.0,\"quantity\":10}";
        Message message = Message.builder()
                .messageId("test-message-id")
                .body(messageBody)
                .receiptHandle("test-receipt-handle")
                .build();

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        when(objectMapper.readValue(eq(messageBody), eq(Product.class))).thenReturn(product);
        when(restApiService.createProduct(product))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, 
            () -> ReflectionTestUtils.invokeMethod(sqsMessageListener, "processMessage", message));
        assertEquals("Failed to process message", thrown.getMessage());
        verify(objectMapper).readValue(eq(messageBody), eq(Product.class));
        verify(restApiService).createProduct(product);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void pollMessages_WithMessages() throws Exception {
        // Arrange
        Message message1 = Message.builder()
                .messageId("message-1")
                .body("{\"name\":\"Product 1\"}")
                .receiptHandle("receipt-1")
                .build();

        Message message2 = Message.builder()
                .messageId("message-2")
                .body("{\"name\":\"Product 2\"}")
                .receiptHandle("receipt-2")
                .build();

        List<Message> messages = Arrays.asList(message1, message2);
        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(messages)
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(response)
                .thenAnswer(invocation -> {
                    latch.countDown();
                    return response;
                });

        Product product = new Product();
        when(objectMapper.readValue(any(String.class), eq(Product.class)))
                .thenReturn(product);
        when(restApiService.createProduct(any(Product.class)))
                .thenReturn(product);

        // Act
        sqsMessageListener.start();
        pollingThread = new Thread(() -> {
            try {
                ReflectionTestUtils.invokeMethod(sqsMessageListener, "pollMessages");
            } catch (Exception e) {
                // Expected to be interrupted
            }
        });
        pollingThread.start();

        // Wait for the first message to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for message processing");

        // Assert
        verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper, atLeastOnce()).readValue(any(String.class), eq(Product.class));
        verify(restApiService, atLeastOnce()).createProduct(any(Product.class));
        verify(sqsClient, atLeastOnce()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void pollMessages_WithEmptyResponse() throws Exception {
        // Arrange
        ReceiveMessageResponse emptyResponse = ReceiveMessageResponse.builder()
                .messages(List.of())
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(emptyResponse)
                .thenAnswer(invocation -> {
                    latch.countDown();
                    return emptyResponse;
                });

        // Act
        sqsMessageListener.start();
        pollingThread = new Thread(() -> {
            try {
                ReflectionTestUtils.invokeMethod(sqsMessageListener, "pollMessages");
            } catch (Exception e) {
                // Expected to be interrupted
            }
        });
        pollingThread.start();

        // Wait for the first poll to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for polling");

        // Assert
        verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper, never()).readValue(any(String.class), eq(Product.class));
        verify(restApiService, never()).createProduct(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void pollMessages_WithException() throws Exception {
        // Arrange
        RuntimeException expectedException = new RuntimeException("SQS Error");
        CountDownLatch errorLatch = new CountDownLatch(1);
        
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(expectedException)
                .thenAnswer(invocation -> {
                    errorLatch.countDown();
                    return ReceiveMessageResponse.builder().messages(List.of()).build();
                });

        // Act
        sqsMessageListener.start();
        pollingThread = new Thread(() -> {
            try {
                ReflectionTestUtils.invokeMethod(sqsMessageListener, "pollMessages");
            } catch (Exception e) {
                // Expected to be interrupted
            }
        });
        pollingThread.start();

        // Wait for the error to be handled and the retry to occur
        assertTrue(errorLatch.await(10, TimeUnit.SECONDS), "Timeout waiting for error handling and retry");

        // Assert
        verify(sqsClient, atLeastOnce()).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper, never()).readValue(any(String.class), eq(Product.class));
        verify(restApiService, never()).createProduct(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void startListening_InitializesPolling() {
        // Act & Assert
        assertDoesNotThrow(() -> sqsMessageListener.startListening());
    }
} 