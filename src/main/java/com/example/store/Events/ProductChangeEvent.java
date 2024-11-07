package com.example.store.Events;

import com.example.store.Models.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductChangeEvent extends ApplicationEvent {

    private final Product productOld;
    private final Product productNew;

    public ProductChangeEvent(Object source, Product productOld, Product productNew) {
        super(source);
        this.productOld = productOld;
        this.productNew = productNew;
    }

}
