package com.tc.application.mocks;

import com.tc.domain.model.Item;
import com.tc.domain.model.Order;
import lombok.experimental.UtilityClass;

import java.util.List;


@UtilityClass
public class Mock {

    public static Order mockOrder() {
       return Order.builder()
                .orderId("ORD123")
                .customerId("CUS001")
                .items(List.of(
                        mockItem("PROD01", 2),
                        mockItem("PROD02", 1)))
                .totalAmount(150.0)
                .build();
    }

    private static Item mockItem(String productId, Integer quantity) {
        return Item.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}
