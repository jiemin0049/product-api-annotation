package com.wiredcoffee.productapiannotation;

import com.wiredcoffee.productapiannotation.controller.ProductController;
import com.wiredcoffee.productapiannotation.model.Product;
import com.wiredcoffee.productapiannotation.model.ProductEvent;
import com.wiredcoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static reactor.core.publisher.Mono.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ProductController.class)
public class JUnit5WebFluxTestAnnotationTest {

    @Autowired
    private WebTestClient client;
    private List<Product> expectedList;

    @MockBean
    private ProductRepository repository;

    @MockBean
    private CommandLineRunner runner;

    @BeforeEach
    public void beforeEach() {
        expectedList = Arrays.asList(
                new Product("1", "Big Latte", 2.99)
        );
    }

    @Test
    public void testProductInvalidIdNotFound() {
        String id = "aaa";
        when(repository.findById(id).thenReturn(Mono.empty()));
        client
                .get()
                .uri("/products/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testProductIdFound() {
        Product p0 = expectedList.get(0);
        when(repository.findById(p0.getId()).thenReturn(Mono.just(p0)));
        client
                .get()
                .uri("/products/{id}", p0.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(p0);
    }

    @Test
    public void testProductEvents() {
        ProductEvent event = new ProductEvent(0L, "Product Event");

        FluxExchangeResult<ProductEvent> result =
                client.get().uri("/products/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(ProductEvent.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(event)
                .expectNextCount(2)
                .consumeNextWith(e ->
                        assertEquals(Long.valueOf(3), e.getEventId()))
                .thenCancel()
                .verify();
    }

    @Test
    public void testGetAllProducts() {
        when(repository.findAll()).thenReturn(Flux.fromIterable((expectedList)));
        client
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }
}
