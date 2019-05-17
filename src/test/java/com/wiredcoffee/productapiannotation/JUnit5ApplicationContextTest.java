package com.wiredcoffee.productapiannotation;

import com.wiredcoffee.productapiannotation.model.Product;
import com.wiredcoffee.productapiannotation.model.ProductEvent;
import com.wiredcoffee.productapiannotation.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JUnit5ApplicationContextTest {
    private WebTestClient client;
    private List<Product> expectedList;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    public void beforeEach() {
        client =
                WebTestClient
                        .bindToApplicationContext(context)
                        .configureClient()
                        .baseUrl("/products")
                        .build();
        expectedList = repository.findAll().collectList().block();
    }

    @Test
    public void testProductInvalidIdNotFound() {
        client
                .get()
                .uri("/aaa")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testProductIdFound() {
        Product p0 = expectedList.get(0);
        client
                .get()
                .uri("/{id}", p0.getId())
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
                client.get().uri("/events")
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
