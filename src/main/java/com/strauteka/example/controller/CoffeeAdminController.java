package com.strauteka.example.controller;


import com.strauteka.example.entity.Coffee;
import com.strauteka.example.service.CoffeeReactiveService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "reactive/coffee", produces = MediaType.APPLICATION_JSON_VALUE)
public class CoffeeAdminController {
    private Optional<Disposable> subscription;
    private final CoffeeReactiveService coffeeReactiveService;

    public CoffeeAdminController(CoffeeReactiveService coffeeReactiveService) {
        this.coffeeReactiveService = coffeeReactiveService;
        this.subscription = Optional.empty();
    }

    @ApiOperation(value = "This method is used to generate data")
    @PostMapping(value = "generate")
    Mono<Long> generateCoffee(@RequestParam(value = "count", defaultValue = "10") Integer count) {

        final Flux<Dummy> dummyFlux = Flux.just("AFFOGATO", "COLD BREW COFFEE", "ESPRESSO")
                .flatMap(n1 -> Flux.just("Rubber", "Cold black", "Bitter")
                        .map(n2 -> Dummy.of(n1, n2))).repeat();

        return Flux.range(0, count)
                .zipWith(dummyFlux,
                        (id, dummy) -> new Coffee(null, dummy.a + id, new Date(), dummy.b + id))
                .flatMap(coffeeReactiveService::save)
                .count();
    }

    @ApiOperation(value = "This method is used to subscribe / unsubscribe on data change for internal log")
    @PostMapping(value = "internal/subscribe")
    synchronized Mono<Boolean> listenChange() {
        if (subscription.isEmpty()) {
            this.subscription = Optional.of(
                    coffeeReactiveService
                            .listenChange()
                            .subscribe(n -> log.info("IncomingCoffee {}", n))
            );
        } else {
            subscription.get().dispose();
            subscription = Optional.empty();
        }
        return Mono.just(subscription.isPresent());
    }

    @ApiOperation(value = "This method is used to delete random data")
    @DeleteMapping(value = "delete")
    Mono<Long> deleteKeys(@RequestParam(value = "count", defaultValue = "10") Long count,
                          @RequestParam(value = "requestSec", defaultValue = "5") Integer requestSec) {
        return coffeeReactiveService
                .getAll(Duration.ofSeconds(requestSec), count, true)
                .flatMap(cafe -> coffeeReactiveService.delete(cafe.getId())).count();
    }

    private static class Dummy {
        public final String a;
        public final String b;

        public Dummy(String a, String b) {
            this.a = a;
            this.b = b;
        }

        public static Dummy of(String a, String b) {
            return new Dummy(a, b);
        }
    }
}
