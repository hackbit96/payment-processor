package com.tc.application.service;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PartitionKey;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.queue.QueueAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tc.domain.model.Item;
import com.tc.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.when;

public class OrderProcessorTest {

    @InjectMocks
    private OrderProcessor orderProcessor;

    @Mock
    private QueueAsyncClient queueAsyncClient;

    @Mock
    private CosmosAsyncContainer cosmosContainer;

    @Mock
    private BlobContainerAsyncClient blobContainerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderProcessor = new OrderProcessor(queueAsyncClient, cosmosContainer, blobContainerClient);

        sampleOrder = new Order(
                UUID.randomUUID().toString(),
                "ORD-001",
                "CUST-001",
                List.of(new Item("PROD-001", 2)),
                250.0
        );
    }

    @Test
    void testProcessMessage_success() {
        String json = toJson(sampleOrder);

        StepVerifier.create(orderProcessor.processMessage(json))
                .expectNextMatches(order -> order.getOrderId().equals("ORD-001"))
                .verifyComplete();
    }

    @Test
    void testProcessMessage_invalidJson() {
        String invalidJson = "{invalid json}";

        StepVerifier.create(orderProcessor.processMessage(invalidJson))
                .expectError()
                .verify();
    }

    @Test
    void testSaveToCosmos_success() {
        when(cosmosContainer.createItem(any(Order.class), any(PartitionKey.class), isNull()))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderProcessor.saveToCosmos(sampleOrder))
                .verifyComplete();
    }


    @Test
    void testDeleteMessage_success() {
        when(queueAsyncClient.deleteMessage(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderProcessor.deleteMessage("message-id", "pop-receipt"))
                .verifyComplete();
    }

    @Test
    void testDeleteMessage_failure() {
        when(queueAsyncClient.deleteMessage(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("delete failed")));

        StepVerifier.create(orderProcessor.deleteMessage("message-id", "pop-receipt"))
                .verifyComplete();
    }


    private String toJson(Order order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}