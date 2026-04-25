package com.example;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.UUID;

import static com.example.demo.jooq.Tables.CUSTOMERS;
import static com.example.demo.jooq.Tables.ORDERS;
import static com.example.demo.jooq.Tables.ORDER_ITEMS;
import static com.example.demo.jooq.Tables.PRODUCTS;

@RequiredArgsConstructor
@Slf4j
@Singleton
public class OrderRepository {
    private final DSLContext dslContext;

    CustomerOrderSummary findOrderSummaryByCustomer(UUID custId) {

        var result = dslContext
                .select(CUSTOMERS.ID, CUSTOMERS.NAME, DSL.count(ORDERS.ID))
                .from(CUSTOMERS.leftJoin(ORDERS).on(CUSTOMERS.ID.eq(ORDERS.CUSTOMER_ID)))
                .where(CUSTOMERS.ID.eq(custId))
                .fetchOptional()
                .map(customer -> new CustomerOrderSummary(customer.value1(), customer.value2(), customer.value3()));

        return result.orElseThrow();

    }

    OrderDetails findOrderDetails(UUID orderId) {
        var result = dslContext
                .select(
                        ORDERS.ID,
                        ORDERS.AMOUNT,
                        CUSTOMERS.NAME,
                        DSL.multiset(
                                DSL.select(PRODUCTS.NAME, PRODUCTS.PRICE, ORDER_ITEMS.QUANTITY)
                                        .from(ORDER_ITEMS.leftJoin(PRODUCTS).on(ORDER_ITEMS.PRODUCT_ID.eq(PRODUCTS.ID)))
                                        .where(ORDER_ITEMS.ORDER_ID.eq(orderId))
                        )
                )
                .from(ORDERS.leftJoin(CUSTOMERS).on(ORDERS.CUSTOMER_ID.eq(CUSTOMERS.ID)))
                .where(ORDERS.ID.eq(orderId))
                .fetchOptional()
                .map(it -> new OrderDetails(
                                it.value1(),
                                it.value2(),
                                it.value3(),
                                it.value4().map(d -> new OrderItemDetails(d.value1(), d.value2(), d.value3()))
                        )
                );

        return result.orElseThrow();

    }
}
