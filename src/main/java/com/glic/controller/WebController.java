package com.glic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

/**
 * REST controller for handling AWS SQS operations.
 * This controller provides endpoints for sending and receiving messages
 * from an AWS SQS queue.
 */
@RestController
@RequestMapping("/api/sqs")
@Slf4j
@Controller
public class WebController {
    
    /**
     * Default constructor for WebController.
     * Required by Spring for controller instantiation.
     */
    public WebController() {
    }
    
    /**
     * AWS SQS client for queue operations.
     */
    @Autowired
    private SqsClient sqsClient;
    
    /**
     * The URL of the SQS queue.
     */
    @Value("${aws.sqs.url}")
    private String queueUrl;
    
    /**
     * Sends a message to the SQS queue.
     *
     * @param message The message to send
     * @return The message ID of the sent message
     */
    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
        
        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        return "Message sent with ID: " + response.messageId();
    }
    
    /**
     * Receives messages from the SQS queue.
     *
     * @return A list of received messages
     */
    @GetMapping("/receive")
    public List<Message> receiveMessages() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();
        
        ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
        return response.messages();
    }
}
