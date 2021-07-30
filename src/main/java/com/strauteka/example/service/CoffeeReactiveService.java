package com.strauteka.example.service;

import com.strauteka.example.channel.CoffeeReactiveChannel;
import com.strauteka.example.entity.Coffee;
import com.strauteka.example.repository.CoffeeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;

import static com.strauteka.example.entity.EntityUtils.combine2Objects;

@Slf4j
@Service
public class CoffeeReactiveService {
    private final CoffeeReactiveRepository coffeeReactiveRepository;
    private final CoffeeReactiveChannel coffeeReactiveChannel;

    public CoffeeReactiveService(CoffeeReactiveRepository coffeeReactiveRepository,
                                 CoffeeReactiveChannel coffeeReactiveChannel) {
        this.coffeeReactiveRepository = coffeeReactiveRepository;
        this.coffeeReactiveChannel = coffeeReactiveChannel;
    }

    public Mono<Coffee> find(Long id) {
        return this.coffeeReactiveRepository.get(id);
    }

    public Flux<Coffee> getAll(Duration duration, Long limit, boolean limitRequest) {
        return this.coffeeReactiveRepository.getAll(duration, limit, limitRequest);
    }

    public Mono<Tuple2<Coffee, Boolean>> delete(Long id) {
        Mono<Tuple2<Coffee, Boolean>> tuple2Mono = this.coffeeReactiveRepository
                .get(id)
                .flatMap(e -> Mono.just(e)
                        .zipWith(this.coffeeReactiveRepository.deleteCoffee(id).map(Tuple2::getT2)));
        return notify(tuple2Mono, true);
    }

    public Mono<Tuple2<Coffee, Boolean>> save(Coffee coffee) {
        if (Objects.isNull(coffee.getId())) {
            if (Objects.isNull(coffee.getCreationTime())) {
                coffee.setCreationTime(new Date());
            }
            return notify(this.coffeeReactiveRepository.saveCoffee(coffee), false);
        } else {
            return this.coffeeReactiveRepository
                    .get(coffee.getId())
                    .map(cafe -> combine2Objects(cafe, coffee))
                    .flatMap(cafe -> notify(this.coffeeReactiveRepository.saveCoffeeWithId(cafe), false));
        }
    }

    public Mono<Long> count() {
        return this.coffeeReactiveRepository.count();
    }

    /**
     * Notify on subscription channel if data alter was successful
     *
     * @param item      Coffee data with success / failure flag
     * @param isDeleted channel change
     * @return same item param
     */
    public Mono<Tuple2<Coffee, Boolean>> notify(Mono<Tuple2<Coffee, Boolean>> item, boolean isDeleted) {
        return item.map((Tuple2<Coffee, Boolean> i) -> {
            if (i.getT2()) {
                // Subscribe forces to evaluate notify
                this.coffeeReactiveChannel.notifyAboutCoffee(i.getT1(), isDeleted).subscribe();
            }
            return i;
        });
    }

    public Flux<Coffee> listenChange() {
        return this.coffeeReactiveChannel.incomingCoffee();
    }
}
