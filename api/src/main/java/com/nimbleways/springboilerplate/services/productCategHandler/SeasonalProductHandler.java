package com.nimbleways.springboilerplate.services.productCategHandler;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.services.implementations.ProductHandler;
import org.springframework.stereotype.Component;


@Component
public class SeasonalProductHandler implements ProductHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public SeasonalProductHandler(ProductRepository productRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    /* Traite un produit SAISONNIER */

    public void handle(Product product) {
        if (product.isInStock() && product.isInSeason()) {
            product.decrementAvailable();
            productRepository.save(product);
        } else if (!product.isInSeason()) {
            notificationService.sendOutOfSeasonNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getLeadTime() > 0) {
            notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
            productRepository.save(product);
        }
    }
}