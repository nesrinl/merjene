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
class SeasonalProductHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private SeasonalProductHandler handler;

    @Test
    void shouldDecrementStockWhenInSeasonAndInStock() {
        // GIVEN — produit en saison avec stock disponible
        Product product = new Product(1L, 15, 5, ProductCateg.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(10), LocalDate.now().plusDays(60));

        // WHEN
        handler.handle(product);

        // THEN
        assertEquals(4, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotifyOutOfStockAndZeroStockWhenRestockExceedsSeason() {
        // GIVEN — leadTime de 100 jours mais la saison se termine dans 30 jours
        Product product = new Product(1L, 100, 0, ProductCateg.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(10), LocalDate.now().plusDays(30));

        // WHEN
        handler.handle(product);

        // THEN
        assertEquals(0, product.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Watermelon");
        verify(productRepository).save(product);
    }


}
