package com.wiredcoffee.productapiannotation;

import com.wiredcoffee.productapiannotation.model.Product;
import com.wiredcoffee.productapiannotation.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiAnnotationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApiAnnotationApplication.class, args);
    }

    CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository repository) {
        return args -> {
            Flux<Product> productFlux = Flux.just(
                    new Product(null, "Big Latte", 2.99),
                    new Product(null, "Big Decaf", 2.49),
                    new Product(null, "Green Tea", 1.99))
                    .flatMap(p -> repository.save(p));

            productFlux
                    .thenMany(repository.findAll())
                    .subscribe(System.out::println);

            /*
            operations.collectionExists(Product.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(exists))
                    .thenMany(v -> operations.createCollection(Product.class))
                    .thenMany(productFlux)
                    .thenMany(repository.findAll())
                    .subscribe(System.out::println);
             */
        };
    }

}
