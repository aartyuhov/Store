package com.example.store.Models;

import java.util.List;

public class ProductListForm {
    private List<Product> products;

    // Default constructor
    public ProductListForm() {
    }

    // Constructor to initialize products
    public ProductListForm(List<Product> products) {
        this.products = products;
    }

    // Getters and setters
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
