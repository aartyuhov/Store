package com.example.store.Components;

import com.example.store.Events.ProductChangeEvent;
import com.example.store.Models.Product;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProductChangeListener {
    private final StoreBot storeBot;

    public ProductChangeListener(StoreBot storeBot) {
        this.storeBot = storeBot;
    }

    @EventListener
    public void handleProductChange(ProductChangeEvent event) {
        Product productOld = event.getProductOld();
        Product productNew = event.getProductNew();
        String result = "Product " + productOld.getArticle() + " was changed!\n" +
                "Before:\n" +
                productOld.showAllDetails() +
                "\nAfter: \n" +
                productNew.showAllDetails();
        if (storeBot.getChatId() != null) {
            storeBot.sendMessage(storeBot.getChatId(),result);
        }
    }
}
