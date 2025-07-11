package com.tc.infrastructure.config;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureCosmosConfig {

    @Value("${azure.cosmos.uri}")
    private String azureCosmosUri;

    @Value("${azure.cosmos.key}")
    private String azureCosmosKey;

    @Value("${azure.cosmos.database-name}")
    private String azureCosmosDatabaseName;

    @Value("${azure.cosmos.container-name}")
    private String azureCosmosContainerName;

    @Value("${azure.cosmos.partition-key-path}")
    private String azureCosmosPartitionKeyPath;

    @Bean
    public CosmosAsyncClient cosmosAsyncClient() {
        return new CosmosClientBuilder()
                .endpoint(azureCosmosUri)
                .key(azureCosmosKey)
                .directMode()
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .gatewayMode()
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    @Bean
    public CosmosAsyncContainer cosmosAsyncContainer(CosmosAsyncClient client) {
        return client.createDatabaseIfNotExists(azureCosmosDatabaseName)
                .flatMap(databaseResponse -> {
                    CosmosAsyncDatabase database = client.getDatabase(azureCosmosDatabaseName);
                    CosmosContainerProperties containerProperties =
                            new CosmosContainerProperties(azureCosmosContainerName, azureCosmosPartitionKeyPath);
                    ThroughputProperties throughput = ThroughputProperties.createManualThroughput(400);
                    return database.createContainerIfNotExists(containerProperties, throughput);
                })
                .map(containerResponse -> client.getDatabase(azureCosmosDatabaseName).getContainer(azureCosmosContainerName))
                .block();
    }
}

