package com.strauteka.example.controller;


import com.strauteka.example.entity.Coffee;
import com.strauteka.example.service.CoffeeReactiveService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
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
    @PostMapping(value = "generate", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<Integer> generateCoffee(ServerWebExchange serverWebExchange,
                                 @RequestParam(value = "count", defaultValue = "1000") Integer count,
                                 @RequestParam(value = "requestMillis", defaultValue = "3000") Integer requestMillis,
                                 @RequestParam(value = "bufferCount", defaultValue = "100") Integer bufferCount) {

        final String logPrefix = serverWebExchange.getLogPrefix();
        log.debug(String.format("%s Preparing to generate data", logPrefix));

        final Flux<Dummy> dummyFlux = Flux.just("AFFOGATO", "COLD BREW COFFEE", "ESPRESSO")
                .flatMap(n1 -> Flux.just("Rubber", "Cold black", "Bitter")
                        .map(n2 -> Dummy.of(n1, n2))).repeat();

        Flux<Integer> map = Flux.range(0, count)
                .zipWith(dummyFlux,
                        (id, dummy) -> new Coffee(null, dummy.a + id, new Date(), dummy.b + id))
                .flatMap(coffeeReactiveService::save)
                .take(Duration.ofMillis(requestMillis))
                .buffer(bufferCount).map(n -> {
                    log.debug(String.format("%s Generating data: %d", logPrefix, n.size()));
                    return n.size();
                });
        return map.doFinally(sign -> log.debug(String.format("%s Data generated", logPrefix)));
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
    @DeleteMapping(value = "delete", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<Integer> deleteKeys(ServerWebExchange serverWebExchange,
                             @RequestParam(value = "count", defaultValue = "1000") Long count,
                             @RequestParam(value = "requestMillis", defaultValue = "3000") Integer requestMillis,
                             @RequestParam(value = "bufferCount", defaultValue = "100") Integer bufferCount) {

        final String logPrefix = serverWebExchange.getLogPrefix();
        log.debug(String.format("%s Preparing to delete data", logPrefix));
        final Duration timespan = Duration.ofMillis(requestMillis);
        Flux<Integer> map = coffeeReactiveService
                .getAll(timespan, count, true)
                .flatMap(cafe -> coffeeReactiveService.delete(cafe.getId()))
                .take(timespan)
                .buffer(bufferCount).map(n -> {
                    log.debug(String.format("%s Deleting data: %d", logPrefix, n.size()));
                    return n.size();
                });
        return map
                .doFinally(sign -> log.debug(String.format("%s Data deleted", logPrefix)));
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
