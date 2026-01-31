package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductCateg;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<Product>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                                .contentType("application/json"))
                                .andExpect(status().isOk());
                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(resultOrder.getId(), order.getId());
        }

        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }
/*
        private static List<Product> createProducts() {
                List<Product> products = new ArrayList<>();
                products.add(new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null));
                products.add(new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null));
                products.add(new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now().plusDays(26), null,
                                null));
                products.add(new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now().minusDays(2), null, null));
                products.add(new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now().minusDays(2),
                                LocalDate.now().plusDays(58)));
                products.add(new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now().plusDays(180),
                                LocalDate.now().plusDays(240)));
                return products;
        }
*/

    @Test
    public void shouldDecrementStockForNormalProductInStock() throws Exception {
        // GIVEN
        Product product = productRepository.save(
                new Product(null, 15, 10, ProductCateg.NORMAL, "USB Cable", null, null, null));
        Order order = orderRepository.save(new Order(null, Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(9, updated.getAvailable());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldNotifyDelayForNormalProductOutOfStock() throws Exception {
        // GIVEN
        Product product = productRepository.save(
                new Product(null, 10, 0, ProductCateg.NORMAL, "USB Dongle", null, null, null));
        Order order = orderRepository.save(new Order(null, Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        verify(notificationService).sendDelayNotification(10, "USB Dongle");
    }

    // --- EXPIRABLE ---

    @Test
    public void shouldDecrementStockForExpirableProductNotExpired() throws Exception {
        // GIVEN
        Product product = productRepository.save(
                new Product(null, 15, 6, ProductCateg.EXPIRABLE, "Butter",
                        LocalDate.now().plusDays(26), null, null));
        Order order = orderRepository.save(new Order(null, Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(5, updated.getAvailable());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldNotifyExpirationForExpiredProduct() throws Exception {
        // GIVEN
        LocalDate pastExpiry = LocalDate.now().minusDays(2);
        Product product = productRepository.save(
                new Product(null, 90, 6, ProductCateg.EXPIRABLE, "Milk", pastExpiry, null, null));
        Order order = orderRepository.save(new Order(null, Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(0, updated.getAvailable());
        verify(notificationService).sendExpirationNotification("Milk", pastExpiry);
    }

    // --- SEASONAL ---

    @Test
    public void shouldDecrementStockForSeasonalProductInSeasonAndInStock() throws Exception {
        // GIVEN
        Product product = productRepository.save(
                new Product(null, 15, 10, ProductCateg.SEASONAL, "Watermelon",
                        null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58)));
        Order order = orderRepository.save(new Order(null, Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(9, updated.getAvailable());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldNotifyOutOfStockForSeasonalProductBeforeSeason() throws Exception {
        // GIVEN — la saison commence dans 180 jours
        Product product = productRepository.save(
                new Product(null, 15, 30, ProductCateg.SEASONAL, "Grapes",
                        null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240)));
        Order order = orderRepository.save(new Order(null, Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        verify(notificationService).sendOutOfStockNotification("Grapes");
    }

    // --- Ordre complet avec plusieurs produits ---

    @Test
    public void processOrderShouldReturn() throws Exception {
        // GIVEN — un ordre avec un mix de types de produits
        Product normal = productRepository.save(
                new Product(null, 15, 30, ProductCateg.NORMAL, "USB Cable IT", null, null, null));
        Product expirable = productRepository.save(
                new Product(null, 15, 30, ProductCateg.EXPIRABLE, "Butter IT",
                        LocalDate.now().plusDays(26), null, null));
        Product seasonal = productRepository.save(
                new Product(null, 15, 30, ProductCateg.SEASONAL, "Watermelon IT",
                        null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58)));
        Order order = orderRepository.save(new Order(null, Set.of(normal, expirable, seasonal)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN — tous les stocks doivent être décrémentés
        assertEquals(29, productRepository.findById(normal.getId()).orElseThrow().getAvailable());
        assertEquals(29, productRepository.findById(expirable.getId()).orElseThrow().getAvailable());
        assertEquals(29, productRepository.findById(seasonal.getId()).orElseThrow().getAvailable());
    }
}
