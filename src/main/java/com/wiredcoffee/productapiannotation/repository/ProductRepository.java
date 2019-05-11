package com.wiredcoffee.productapiannotation.repository;

import com.wiredcoffee.productapiannotation.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    //Flux<Product> findbyName(String name);
}
