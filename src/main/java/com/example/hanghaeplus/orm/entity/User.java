package com.example.hanghaeplus.orm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {


    @Id
    @Column(name = "user_id")
    private Long id;
    private String name;

}