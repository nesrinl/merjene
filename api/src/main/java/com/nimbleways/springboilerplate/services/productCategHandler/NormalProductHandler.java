package com.nimbleways.springboilerplate.services.productCategHandler;

import com.nimbleways.springboilerplate.services.implementations.ProductHandler;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.springframework.stereotype.Component;



@Component
public class NormalProductHandler implements ProductHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public NormalProductHandler(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    /*Traiter un produit NORMAL.*/
    public void handleProduct(Product product) {
        if (product.isInStock()) {
            product.decrementAvailable();
            productRepository.save(product);
        } else if (product.getLeadTime() > 0) {
            notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
            productRepository.save(product);
        }
    }



}