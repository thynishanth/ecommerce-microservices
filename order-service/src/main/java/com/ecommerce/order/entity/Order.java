package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    @SequenceGenerator(name = "orders_seq", sequenceName = "orders_seq", allocationSize = 1)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "reserved_batch_ids")
    private String reservedBatchIds;
}
