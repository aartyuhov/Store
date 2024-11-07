package com.example.store.Services;

import com.example.store.Events.ProductChangeEvent;
import com.example.store.Models.Product;
import com.example.store.Repo.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    List<Product> productsToUpload;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public ProductService(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {

        this.productRepository = productRepository;
        this.productsToUpload = new ArrayList<>();
        this.eventPublisher = eventPublisher;

    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAllAvailableProducts() {
        return productRepository.findByAvailable(true);
    }

    public List<Product> getAllProductsToUpload() {
        return productsToUpload;
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
                    //check if any properties were changed-if true - activate Event
                    if (hasDifferences(existingProduct, product)) {
                        eventPublisher.publishEvent(new ProductChangeEvent(this, existingProduct, product));
                    }
                    existingProduct.setArticle(product.getArticle());
                    existingProduct.setName(product.getName());
                    existingProduct.setCategory(product.getCategory());
                    existingProduct.setBrand(product.getBrand());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setQuantity(product.getQuantity());
                    existingProduct.setPack(product.getPack());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setImageUrl(product.getImageUrl());
                    existingProduct.setAvailable(product.getAvailable());
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found!"));

    }

    public static boolean hasDifferences(Object obj1, Object obj2) {
        for (Field field : obj1.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);
                if (!Objects.equals(value1, value2)) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<Product> findProductByArticle(Integer article) {
        return productRepository.findByArticle(article);
    }

    public List<Product> findProductToUploadByArticle(Integer article) {
        return productsToUpload.stream()
                .filter(product -> product.getArticle().equals(article))
                .collect(Collectors.toList());
    }

    public List<Product> readExcelFile(String excelFilePath) throws IOException {

        productsToUpload = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {  // Skip header row
                Row row = sheet.getRow(i);
                if ((row != null)) {

                    Integer article  = convertStringToInteger(getCellValue(row.getCell(0)));
                    String name = getCellValue(row.getCell(1));
                    String category = getCellValue(row.getCell(2));
                    String brand = getCellValue(row.getCell(3));
                    Double price = convertStringToDouble(getCellValue(row.getCell(4)));
                    Integer quantity = convertStringToInteger(getCellValue(row.getCell(5)));
                    Integer pack = convertStringToInteger(getCellValue(row.getCell(6)));
                    String description = getCellValue(row.getCell(7));
                    String imageUrl = getCellValue(row.getCell(8));

                    if (article != null) {
                        Product product = new Product(article,name,category,brand,price,quantity,pack,description,imageUrl,true);
                        productsToUpload.add(product);
                    }
                }

            }
        }
        return productsToUpload;
    }

    public byte[] writeExcelFile(List<Integer> selectedProductArticles) {

        List<Product> productsToDownload = getAllProducts().stream()
                .filter(product -> selectedProductArticles.contains(product.getArticle()))
                .toList();


        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Store");
            Row headerRow = sheet.createRow(0);

            // Define header cells
            String[] headers = {"article", "name", "category", "brand", "price", "quantity", "pack", "description", "imageUrl"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Fill data rows
            int rowIdx = 1;
            for (Product product : productsToDownload) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(product.getArticle());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getCategory());
                row.createCell(3).setCellValue(product.getBrand());
                row.createCell(4).setCellValue(product.getPrice());
                row.createCell(5).setCellValue(product.getQuantity());
                row.createCell(6).setCellValue(product.getPack());
                row.createCell(7).setCellValue(product.getDescription());
                row.createCell(8).setCellValue(product.getImageUrl());
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    public boolean isProductsUpload(List<Integer> selectedProductArticles) {
        if ((selectedProductArticles == null) || (selectedProductArticles.isEmpty())) {
            return false;
        }

        if ((productsToUpload == null) || (productsToUpload.isEmpty())) {
            return false;
        }

        List<Product> productsSelectedToUpload = productsToUpload.stream()
                .filter(product -> selectedProductArticles.contains(product.getArticle()))
                .collect(Collectors.toList());

        //Reset availability to false
        for (Product product : getAllProducts()) {
            product.setAvailable(false);
        }

        //Add and update products in store

        for (Product product : productsSelectedToUpload) {
            product.setAvailable(true);
            if (!findProductByArticle(product.getArticle()).isEmpty()) {
                for (Product productFindByArticle : findProductByArticle(product.getArticle())) {
                    updateProduct(productFindByArticle.getId(), product);
                }
            } else {
                addProduct(product);
            }
        }

        //update status - not available
//        updateProductsAvailability(productsSelectedToUpload, getAllProducts());
        return true;
    }

    private static String getCellValue(Cell cell) {
        // Get the cell value based on its type
        if (cell == null) {
            return null; // Return a string indicating null for null cells
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula(); // Return the formula as a string
            case ERROR -> null; // Handle error cells
            case BLANK -> null; // Return empty string for blank cells
            default -> null;
        };
    }

    public static Integer convertStringToInteger(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return Math.toIntExact(Math.round(Double.parseDouble(str)));
        } catch (Exception e) {
            return null;
        }
    }

    public static Double convertStringToDouble(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return Math.floor(Double.parseDouble(str) * 1000) / 1000;
        } catch (Exception e) {
            return null;
        }
    }


}
