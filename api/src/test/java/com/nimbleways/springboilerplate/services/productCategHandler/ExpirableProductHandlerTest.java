package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductCateg;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(SpringExtension.class)
@UnitTest
class ExpirableProductHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ExpirableProductHandler handler;



    @Test
    void shouldNotifyExpirationWhenExpiryDateIsToday() {
        // GIVEN — la date d'expiration est aujourd'hui (le produit est considéré comme expiré)
        LocalDate today = LocalDate.now();
        Product product = new Product(1L, 15, 3, ProductCateg.EXPIRABLE, "Yogurt", today, null, null);

        // WHEN
        handler.handle(product);

        // THEN
        assertEquals(0, product.getAvailable());
        verify(notificationService).sendExpirationNotification("Yogurt", today);
        verify(productRepository).save(product);
    }
}