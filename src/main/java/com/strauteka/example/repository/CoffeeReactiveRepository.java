package com.strauteka.example.repository;

import com.strauteka.example.entity.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.annotation.PostConstruct;
import java.time.Duration;

import static com.strauteka.example.entity.EntityUtils.combine2Objects;

@Slf4j
@Repository
public class CoffeeReactiveRepository {
    // TODO: export to separate config
    private static final String COFFEE_ID_SEQ = "COFFEE:SEQ";
    public static final String COFFEE_PREFIX = "COFFEE:ID:";
    public static final String COFFEE_ALL = COFFEE_PREFIX + "*";

    private final ReactiveRedisOperations<String, Coffee> coffeeOps;
    private final ReactiveRedisOperations<String, Long> coffeeSeq;


    public CoffeeReactiveRepository(ReactiveRedisOperations<String, Coffee> coffeeOps,
                                    ReactiveRedisOperations<String, Long> coffeeSeq) {
        this.coffeeOps = coffeeOps;
        this.coffeeSeq = coffeeSeq;
    }

    @PostConstruct
    public void initSeq() {
        coffeeSeq.opsForValue().setIfAbsent(COFFEE_ID_SEQ, 0L).subscribe();
    }

    public Flux<Coffee> getAll(Duration duration, Long limit, boolean limitRequest) {
        return coffeeOps
                .keys(COFFEE_ALL)
                .take(duration)
                .take(limit, limitRequest)
                .flatMap(coffeeOps.opsForValue()::get);
    }

    public Mono<Coffee> get(Long id) {
        //todo: custom Error handling
        return coffeeOps.opsForValue().get(COFFEE_PREFIX + id)
                .switchIfEmpty(Mono.error(new RuntimeException(String.format("Coffee with Id: %d not found!", id))));
    }

    public Mono<Tuple2<Coffee, Boolean>> saveCoffee(Coffee coffee) {
        return coffeeSeq
                .opsForValue()
                .increment(COFFEE_ID_SEQ)
                .map(id -> {
                    Coffee cafe = new Coffee();
                    cafe.setId(id);
                    return combine2Objects(cafe, coffee);
                })
                .flatMap(this::saveCoffeeWithId);
    }

    public Mono<Tuple2<Coffee, Boolean>> saveCoffeeWithId(Coffee coffee) {
        return Mono.just(coffee)
                .zipWith(coffeeOps.opsForValue().set(COFFEE_PREFIX + coffee.getId().toString(), coffee));
    }

    public Mono<Tuple2<Long, Boolean>> deleteCoffee(Long id) {
        return Mono.just(id).zipWith(coffeeOps.opsForValue().delete(COFFEE_PREFIX + id));
    }

    public Mono<Long> count() {
        return coffeeOps.keys(COFFEE_ALL).count();
    }
}
