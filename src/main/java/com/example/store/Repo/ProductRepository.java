package com.example.store.Repo;

import com.example.store.Models.Product;
import com.example.store.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByArticle(Integer article);
    Optional<Product> findProductById(Long id);
    void deleteProductByArticle(Integer article);

}
