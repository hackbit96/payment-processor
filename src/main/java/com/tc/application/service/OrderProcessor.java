package com.tc.application.service;

import com.azure.core.util.BinaryData;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PartitionKey;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.queue.QueueAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tc.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessor {

    @Value("${audit.file.success}")
    private String auditFileSuccess;

    @Value("${audit.file.error}")
    private String auditFileError;

    @Value("${azure.queue.max-messages}")
    private Integer azureQueueMaxMessages;

    private final QueueAsyncClient queueAsyncClient;
    private final CosmosAsyncContainer cosmosContainer;
    private final BlobContainerAsyncClient blobContainerClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Scheduled(fixedDelay = 10000)
    public void pollQueue() {
        log.info("Obteniendo mensajes de la cola...");
        queueAsyncClient.receiveMessages(azureQueueMaxMessages)
                .flatMap(message -> {
                    String messageId = message.getMessageId();
                    String popReceipt = message.getPopReceipt();
                    String rawBody = message.getBody().toString();

                    return processMessage(rawBody)
                            .flatMap(order ->
                                    saveToCosmos(order)
                                            .then(saveToBlob(order))
                                            .thenReturn(true))
                            .onErrorResume(e -> {
                                log.error("Error al procesar el mensaje, no lo eliminaré: {}", rawBody, e);
                                return traceError(rawBody, e)
                                        .thenReturn(false);
                            })
                            .flatMap(success -> {
                                if (success) {
                                    return deleteMessage(messageId, popReceipt);
                                } else {
                                    return Mono.empty();
                                }
                            });
                })
                .subscribe(
                        success -> log.info("Mensaje de procesamiento finalizado"),
                        error -> log.error("Error en el proceso", error)
                );
    }

    public Mono<Order> processMessage(String rawBody) {
        try {
            Order order = objectMapper.readValue(rawBody, Order.class);
            return Mono.just(order);
        } catch (Exception e) {
            log.error("No se pudo deserializar el mensaje", e);
            return Mono.error(e);
        }
    }

    public Mono<Void> saveToCosmos(Order order) {
        return cosmosContainer.createItem(order, new PartitionKey(order.getCustomerId()), null)
                .doOnSuccess(resp -> log.info("Guardado en Cosmos DB: {}", order.getOrderId()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(10)))
                .then();
    }

    public Mono<Void> saveToBlob(Order order) {
        String fileName = String.format(auditFileSuccess, order.getOrderId(), getDate(), UUID.randomUUID());
        return blobContainerClient.getBlobAsyncClient(fileName)
                .upload(BinaryData.fromObject(order), true)
                .doOnSuccess(resp -> log.info("Guardado en el almacenamiento de blobs: {}", fileName))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(10)))
                .then();
    }

    public Mono<Void> deleteMessage(String messageId, String popReceipt) {
        return queueAsyncClient.deleteMessage(messageId, popReceipt)
                .doOnSuccess(resp -> log.info("Mensaje eliminado de la cola"))
                .onErrorResume(e -> {
                    log.error("No se pudo eliminar el mensaje de la cola", e);
                    return Mono.empty();
                });
    }

    public Mono<Void> traceError(String rawMessage, Throwable error) {
        String traceFile = String.format(auditFileError, UUID.randomUUID());
        String errorDetails = "Error: " + error.getMessage() + "\n\nMessage:\n" + rawMessage;

        return blobContainerClient.getBlobAsyncClient("errors/" + traceFile)
                .upload(BinaryData.fromString(errorDetails), true)
                .doOnSuccess(resp -> log.info("Se guardó el seguimiento de errores en Blob: {}", traceFile))
                .onErrorResume(e -> {
                    log.error("No se pudo guardar el seguimiento de errores en Blob", e);
                    return Mono.empty();
                })
                .then();
    }

    private String getDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmm");
        return now.format(formatter);
    }

}
