package com.example.store.Controllers;

import com.example.store.Models.Product;
import com.example.store.Repo.ProductRepository;
import com.example.store.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.Long.parseLong;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }

    @GetMapping("/upload")
    public String showUploadPage(Model model) {
        return "upload";
    }

    @PostMapping("/upload" )
    public String handleFileUpload(@RequestParam String filePath, Model model) {
        // Check if the file is not empty
        if (filePath.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "upload";
        }
        try {
            model.addAttribute("products", productService.readExcelFile(filePath));
            return "products";
        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file", e);
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
    public String searchProduct(@RequestParam Integer query, Model model) {
        List<Product> findProductByArticle = productService.findProductByArticle(query);
        if ((findProductByArticle != null) && (!findProductByArticle.isEmpty())) {
            model.addAttribute("products", findProductByArticle);
            return "/products";
        };

        return "/products";
    }


}
