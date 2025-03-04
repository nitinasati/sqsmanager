package com.glic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/sqs")
@Slf4j
public class WebController {
    
    @Autowired
    private SqsClient sqsClient;
    
    @Value("${aws.sqs.url}")
    private String queueUrl;
    
    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
        
        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        return "Message sent with ID: " + response.messageId();
    }
    
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
