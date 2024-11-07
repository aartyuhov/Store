package com.example.store.Models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
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
    @Getter
    private Boolean available;

    public Product(Integer article, String name, String category, String brand, Double price, Integer quantity,
                   Integer pack, String description, String imageUrl, Boolean available) {
        this.article = article;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.pack = pack;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = true;
    }

    public String showAllDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Product Information:\n");
        sb.append("Article: ").append(article).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Category: ").append(category).append("\n");
        sb.append("Brand: ").append(brand).append("\n");
        sb.append("Price: $").append(price).append("\n");
        sb.append("Quantity: ").append(quantity).append("\n");
        sb.append("Pack: ").append(pack).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Image URL: ").append(imageUrl).append("\n");
        sb.append("Available: ").append(available ? "Yes" : "No").append("\n");

        return sb.toString();
    }

}
