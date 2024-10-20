package com.example.store.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer article;
    private String name;
    private String category;
    private String brand;
    private Double price;
    private Integer quantity;
    private Integer pack;
    private String description;
    private String imageUrl;
    private boolean available;

    public Product(int article, String name, String category, String brand, Double price, int quantity,
                   int pack, String description, String imageUrl, boolean available) {
        this.available = false;
    }
}
