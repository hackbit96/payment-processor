package com.tc.config;

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
    private String uri;

    @Value("${azure.cosmos.key}")
    private String key;

    private static final String DATABASE_NAME = "orders-db";
    private static final String CONTAINER_NAME = "orders";
    private static final String PARTITION_KEY_PATH = "/customerId";


    @Bean
    public CosmosAsyncClient cosmosAsyncClient() {
        return new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .directMode() // Uso directo (más rápido en local)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .gatewayMode() // para evitar problemas con el emulador si Direct falla
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    /*
    @Bean
    public CosmosAsyncContainer cosmosAsyncContainer(CosmosAsyncClient client) {
        return client.getDatabase("orders-db").getContainer("orders");
    }

     */

    @Bean
    public CosmosAsyncContainer cosmosAsyncContainer(CosmosAsyncClient client) {
        return client.createDatabaseIfNotExists(DATABASE_NAME)
                .flatMap(databaseResponse -> {
                    CosmosAsyncDatabase database = client.getDatabase(DATABASE_NAME);
                    CosmosContainerProperties containerProperties =
                            new CosmosContainerProperties(CONTAINER_NAME, PARTITION_KEY_PATH);

                    // Puedes ajustar el throughput (por defecto: 400 RU/s)
                    ThroughputProperties throughput = ThroughputProperties.createManualThroughput(400);

                    return database.createContainerIfNotExists(containerProperties, throughput);
                })
                .map(containerResponse -> {
                    return client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
                })
                .block(); // 👈 bloqueamos para esperar a que esté listo
    }
}

