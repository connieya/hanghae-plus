package com.example.hanghaeplus.orm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Product {
    @Id
    @Column(name = "product_id")
    private Long id;
    private String name;

    private Long stockId;
}
