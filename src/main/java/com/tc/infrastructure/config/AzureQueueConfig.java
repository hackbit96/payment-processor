package com.tc.infrastructure.config;

import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureQueueConfig {

    @Value("${azure.queue.connection-string}")
    private String azureQueueConnectionString;

    @Value("${azure.queue.name}")
    private String azureQueueName;

    @Bean
    public QueueAsyncClient queueAsyncClient() {
        return new QueueClientBuilder()
                .connectionString(azureQueueConnectionString)
                .queueName(azureQueueName)
                .buildAsyncClient();
    }
}