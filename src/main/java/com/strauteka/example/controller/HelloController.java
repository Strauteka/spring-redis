package com.strauteka.example.controller;

import com.strauteka.example.entity.Pong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping()
public class HelloController {

    private final SimpleDateFormat simpleDateFormat;

    public HelloController(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    @GetMapping("hello/{name}")
    Mono<String> hello(@PathVariable("name") String name) {
        return Mono.just(String.format("Hello, %s!", name));
    }

    @GetMapping(value = "hello/flux/{times}/{delay}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<String> helloFlux(@PathVariable("times") Integer times,
                           @PathVariable("delay") Integer delay) {
        return Flux
                .generate(() -> 1L, (state, sink) -> {
                    sink.next(state);
                    return state + 1L;
                }).take(times)
                .delayElements(Duration.ofMillis(delay))
                .map(n -> {
                    log.warn("creating item " + n);
                    return "some_" + n + "\n";
                });
    }

    @GetMapping(value = "ping/{times}/{delay}", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<Pong> pong(@PathVariable("times") Integer times,
                    @PathVariable("delay") Integer delay) {
        return Flux
                .generate(() -> 1L, (state, sink) -> {
                    sink.next(state);
                    return state + 1L;
                }).take(times)
                .delayElements(Duration.ofMillis(delay))
                .map(n -> {
                    log.warn("creating Pong {} - {}", n, simpleDateFormat.format(new Date()));
                    return new Pong("Pong" + n);
                });
    }

    @GetMapping(value = "ping/buffered/{times}/{delay}/{buffer}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Flux<List<Pong>> pongBuf(@PathVariable("times") Integer times,
                             @PathVariable("delay") Integer delay,
                             @PathVariable("buffer") Integer buffer) {
        return pong(times, delay).buffer(buffer);
    }
}
