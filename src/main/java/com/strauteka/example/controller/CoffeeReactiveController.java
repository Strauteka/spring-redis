package com.strauteka.example.controller;

import com.strauteka.example.entity.Coffee;
import com.strauteka.example.service.CoffeeReactiveService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;

import static com.strauteka.example.controller.ControllerUtils.mediaTypeCheck;

@Slf4j
@RestController
@RequestMapping(path = "reactive/coffee", produces = MediaType.APPLICATION_JSON_VALUE)
public class CoffeeReactiveController {

    private final CoffeeReactiveService coffeeReactiveService;

    CoffeeReactiveController(CoffeeReactiveService coffeeReactiveService) {
        this.coffeeReactiveService = coffeeReactiveService;
    }

    @ApiOperation(value = "Coffee data. Output contentType adjusted by Accept(Response Content Type) parameter")
    @GetMapping(value = "", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<Flux<Coffee>> getAllReactiveCoffee(
            @ApiParam(hidden = true) @RequestHeader(value = "Accept", required = false) String acceptContentType,
            @RequestParam(value = "requestSec", defaultValue = "3") Integer requestSec,
            @RequestParam(value = "limit", defaultValue = "1000") Long limit,
            @RequestParam(value = "limitRequest", defaultValue = "true") Boolean limitRequest
    ) {
        MediaType mediaType = mediaTypeCheck(acceptContentType,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_NDJSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.ALL_VALUE);
        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(
                        coffeeReactiveService
                                .getAll(Duration.ofSeconds(requestSec), limit, limitRequest)
                );
    }

    @ApiOperation(value = "Coffee Buffered data. Output contentType adjusted by Accept(Response Content Type) parameter")
    @GetMapping(value = "buffered", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<Flux<List<Coffee>>> allBuffered(
            @ApiParam(hidden = true) @RequestHeader(value = "Accept", required = false) String acceptContentType,
            @RequestParam(value = "requestSec", defaultValue = "3") Long requestSec,
            @RequestParam(value = "limit", defaultValue = "1000") Long limit,
            @RequestParam(value = "limitRequest", defaultValue = "true") Boolean limitRequest,
            @RequestParam(value = "bufferedSize", defaultValue = "200") Integer bufferedSize
    ) {
        MediaType mediaType = mediaTypeCheck(acceptContentType,
                MediaType.APPLICATION_NDJSON,
                MediaType.APPLICATION_NDJSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.ALL_VALUE);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(
                        this.coffeeReactiveService
                                .getAll(Duration.ofSeconds(requestSec), limit, limitRequest)
                                .buffer(bufferedSize)
                );
    }

    @GetMapping("{id}")
    Mono<Coffee> findReactiveCoffee(@PathVariable("id") Long id) {
        return coffeeReactiveService.find(id);
    }

    @PostMapping()
    Mono<Tuple2<Coffee, Boolean>> updateCoffee(
            @RequestBody Coffee coffee) {
        return coffeeReactiveService.save(coffee);
    }

    @DeleteMapping("{id}")
    Mono<Tuple2<Coffee, Boolean>> deleteCoffee(@PathVariable("id") Long id) {
        return coffeeReactiveService.delete(id);
    }

    @PostMapping("{name}/{flavor}")
    Mono<Tuple2<Coffee, Boolean>> saveCoffee(@PathVariable("name") @NonNull String name,
                                             @PathVariable("flavor") @NonNull String flavor) {
        return coffeeReactiveService.save(new Coffee(null, name, null, flavor));
    }

    @GetMapping("count")
    Mono<Long> countCoffee(ServerWebExchange serverWebExchange) {
        final String logPrefix = serverWebExchange.getLogPrefix();
        log.debug(String.format("%s Preparing Count response", logPrefix));
        return coffeeReactiveService.count();
    }

    @GetMapping(value = "sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Coffee> listenChange() {
        return coffeeReactiveService.listenChange();
    }
}
