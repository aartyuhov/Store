package com.example.store.Controllers;

import com.example.store.Models.Product;
import com.example.store.Services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.Long.parseLong;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private final ProductService productService;
    private final String UPLOAD_DIR = "uploads";

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String getAllAvailableProducts(Model model) {
        model.addAttribute("products", productService.getAllAvailableProducts());
        return "products";
    }

    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        return "upload";
    }

    @PostMapping("/upload" )
    public String handleFileUpload(@RequestParam("fileName") MultipartFile fileName, Model model) {
        // Check if the file is not empty
        if (fileName.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "upload";
        }
        String absolutePathString;
        try {
            // Ensure the upload directory exists
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
            }

            // Create a path for the file to be saved
            Path filePath = Path.of(UPLOAD_DIR + fileName.getOriginalFilename());
            absolutePathString = filePath.toAbsolutePath().toString();

            // Move the file to the desired directory
            Files.copy(fileName.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return "Failed to upload file: " + e.getMessage();
        }


        try {
//            model.addAttribute("productsToUpload", new ProductListForm(productService.readExcelFile(absolutePathString)));
            model.addAttribute("productsToUpload", productService.readExcelFile(absolutePathString));
            return "uploadProducts";

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file", e);
        }

    }

    @PostMapping("/productsToUpload")
    public String submitProductsToUpload(@RequestParam(value = "selectedProducts", required = false) List<Integer> selectedProductArticles, Model model) {

        if (selectedProductArticles != null) {
            if (productService.isProductsUpload(selectedProductArticles)) {
                model.addAttribute("status", "Success");
                model.addAttribute("message", "Products uploaded successfully!");
                model.addAttribute("count", selectedProductArticles.size()); // Add the number of uploaded products
            } else {
                model.addAttribute("status", "Error");
                model.addAttribute("message", "Products not uploaded!");
                model.addAttribute("count", selectedProductArticles.size());
            }
        } else {
            model.addAttribute("status", "Error");
            model.addAttribute("message", "Please select a product to upload.");
            model.addAttribute("count", 0);
        }
        return "successUpload";
    }

    @GetMapping("/download")
    public String getAllAvailableProductsToDownload(Model model) {
        model.addAttribute("productsToDownload", productService.getAllAvailableProducts());
        return "downloadProducts";
    }

    @PostMapping("/productsToDownload")
    public ResponseEntity<byte[]> submitProductsToDownload(@RequestParam(value = "selectedProducts", required = false) List<Integer> selectedProductArticles) {

        byte[] excelData = new byte[0];
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Store_"+
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+".xlsx");

        if (selectedProductArticles != null) {

            excelData = productService.writeExcelFile(selectedProductArticles);
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } else {
            return new ResponseEntity<>(excelData, headers, HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/add")
    public String addProduct(Model model) {
        return "addProduct";
    }

    @PostMapping("/add")
    public String addNewProduct(@ModelAttribute Product product) {
        productService.addProduct(product);
        return "redirect:/products/";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        if (!productService.existsById(id)) {
            return "redirect:/products/";
        }
        productService.deleteProduct(id);
        return "redirect:/products/";
    }

    @GetMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, Model model) {
        if (!productService.existsById(id)) {
            return "redirect:/products/";
        }
        Product product = productService.findProductById(id).get();
        model.addAttribute("product", product);
        return "update";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product) {
        if (!productService.existsById(id)) {
            return "redirect:/products/";
        }
        productService.updateProduct(id, product);
        return "redirect:/products/";
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam(required = false) Integer query, Model model, HttpServletRequest request) {
        String previousPath = request.getHeader("Referer");
        List<Product> findProductByArticle;
        if (previousPath.equals("http://localhost:8080/products/upload")) {
            findProductByArticle = productService.findProductToUploadByArticle(query);
            if ((findProductByArticle != null) && (!findProductByArticle.isEmpty())) {
                model.addAttribute("productsToUpload", findProductByArticle);
                return "uploadProducts";
            };
            model.addAttribute("productsToUpload", productService.getAllProductsToUpload());
            return "uploadProducts";
        }
        findProductByArticle = productService.findProductByArticle(query);
        if ((findProductByArticle != null) && (!findProductByArticle.isEmpty())) {
            model.addAttribute("products", findProductByArticle);
            return "/products";
        };
        model.addAttribute("products", productService.getAllAvailableProducts());
        return "/products";
    }


}
