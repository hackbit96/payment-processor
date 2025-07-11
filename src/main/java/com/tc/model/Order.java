package com.tc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @JsonProperty("id")
    private String id = UUID.randomUUID().toString();

    private String orderId;
    private String customerId;
    private List<Item> items;
    private Double totalAmount;
}
