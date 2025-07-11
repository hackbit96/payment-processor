package com.tc.service;

import com.azure.core.util.BinaryData;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PartitionKey;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.queue.QueueAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tc.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessor {

    private final QueueAsyncClient queueAsyncClient;
    private final CosmosAsyncContainer cosmosContainer;
    private final BlobContainerAsyncClient blobContainerClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Scheduled(fixedDelay = 10000)
    public void pollQueue() {
        log.info("Polling messages from queue...");
        queueAsyncClient.receiveMessages(10)
                .doOnNext(message -> log.info("Received message: " + message.getBody().toString()))
                .flatMap(message -> {
                    Order order;
                    try {
                        order = objectMapper.readValue(message.getBody().toString(), Order.class);
                    } catch (Exception e) {
                        log.error("Failed to parse message: " + message.getBody().toString(), e);
                        return Mono.empty();
                    }
                    return cosmosContainer.createItem(order, new PartitionKey(order.getCustomerId()), null)
                            .doOnSuccess(resp -> log.info("Saved to Cosmos DB: {}", order.getOrderId()))
                            .then(blobContainerClient
                                    .getBlobAsyncClient("audit_" + order.getOrderId() + ".json")
                                    .upload(BinaryData.fromObject(order), true)
                                    .doOnSuccess(resp -> log.info("Saved to Blob Storage")))
                            .then(queueAsyncClient.deleteMessage(message.getMessageId(), message.getPopReceipt())
                                    .doOnSuccess(v -> log.info("Deleted message from queue")));
                })
                .subscribe(
                        unused -> log.info("Message processed"),
                        error -> log.error("Error processing message", error)
                );

    }
}
