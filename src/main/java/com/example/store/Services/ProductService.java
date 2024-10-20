package com.example.store.Services;

import com.example.store.Models.Product;
import com.example.store.Models.User;
import com.example.store.Repo.ProductRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findProductById(id);
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    public void addProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public void updateProduct(Long id, Product product) {
        productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setArticle(product.getArticle());
                    existingProduct.setName(product.getName());
                    existingProduct.setCategory(product.getCategory());
                    existingProduct.setBrand(product.getBrand());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setQuantity(product.getQuantity());
                    existingProduct.setPack(product.getPack());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setImageUrl(product.getImageUrl());
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found!"));
    }

    public List<Product> findProductByArticle(Integer article) {
        return productRepository.findByArticle(article);
    }

    public void deleteProductsNotInProducts1ByArticle(List<Product> products1, List<Product> products2) {
        // Extract articles from products1 (the list we want to keep)
        Set<Integer> articlesInProducts1 = products1.stream()
                .map(Product::getArticle)
                .collect(Collectors.toSet());

        // Find products in products2 that are NOT in products1 based on the article field
        List<Integer> articlesToDelete = products2.stream()
                .map(Product::getArticle)
                .filter(article -> !articlesInProducts1.contains(article))
                .toList();

        // Delete products by their article number
        for (Integer article : articlesToDelete) {
            productRepository.deleteProductByArticle(article);
        }
    }

    public List<Product> readExcelFile(String excelFilePath) throws IOException {
        List<Product> products = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {  // Skip header row
                Row row = sheet.getRow(i);
                int article = (int) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                String category = row.getCell(2).getStringCellValue();
                String brand = row.getCell(3).getStringCellValue();
                Double price = row.getCell(4).getNumericCellValue();
                int quantity = (int) row.getCell(5).getNumericCellValue();
                int pack = (int) row.getCell(6).getNumericCellValue();
                String description = row.getCell(7).getStringCellValue();
                String imageUrl = row.getCell(8).getStringCellValue();
                Product product = new Product(article,name,category,brand,price,quantity,pack,description,imageUrl,false);
                if (!findProductByArticle(article).isEmpty()) {
                    for (Product productFindByArticle : findProductByArticle(article)) {
                        updateProduct(productFindByArticle.getId(), productFindByArticle);
                    }
                } else {
                    products.add(product);
                }
            }
        }

        deleteProductsNotInProducts1ByArticle(products,getAllProducts());

        return products;
    }
}
