package com.tc.infrastructure.config;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    @Value("${azure.blob.name}")
    private String azureBlobName;

    @Bean
    public BlobContainerAsyncClient blobContainerClient() {
        return new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(azureBlobName)
                .buildAsyncClient();
    }
}