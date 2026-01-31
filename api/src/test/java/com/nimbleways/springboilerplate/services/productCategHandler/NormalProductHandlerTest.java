package com.nimbleways.springboilerplate.services.productCategHandler;


import com.nimbleways.springboilerplate.entities.ProductCateg;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(SpringExtension.class)
@UnitTest
class NormalProductHandlerTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NormalProductHandler handler;

    @Test
    void shouldDecrementStockWhenProductIsInStock() {
        // GIVEN
        Product product = new Product(1L, 15, 10, ProductCateg.NORMAL, "USB Cable", null, null, null);

        // WHEN
        handler.handle(product);

        // THEN
        assertEquals(9, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoMoreInteractions(notificationService);
    }


}
