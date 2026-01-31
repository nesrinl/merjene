package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductCateg;
import com.nimbleways.springboilerplate.services.productCategHandler.ExpirableProductHandler;
import com.nimbleways.springboilerplate.services.productCategHandler.NormalProductHandler;
import com.nimbleways.springboilerplate.services.productCategHandler.SeasonalProductHandler;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ProductService {

    private final Map<ProductCateg, ProductHandler> handlers;

    public ProductService(NormalProductHandler normalHandler,SeasonalProductHandler seasonalHandler,ExpirableProductHandler expirableHandler) {
        this.handlers = Map.of(
                ProductCateg.NORMAL, normalHandler,
                ProductCateg.SEASONAL, seasonalHandler,
                ProductCateg.EXPIRABLE, expirableHandler
        );
    }

    public void processProduct(Product product) {
        ProductHandler handler = handlers.get(product.getType());
        if (handler == null) {
            throw new IllegalArgumentException("Type de produit est inconnu : " + product.getType());
        }
        handler.handleProduct(product);
    }
}