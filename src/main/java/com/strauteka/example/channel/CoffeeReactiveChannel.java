package com.strauteka.example.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strauteka.example.entity.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
public class CoffeeReactiveChannel {

    private static final ChannelTopic COFFEE_TOPIC = ChannelTopic.of("COFFEE:QUEUE");
    private static final ChannelTopic COFFEE_DELETE_TOPIC = ChannelTopic.of("COFFEE:DELETE:QUEUE");


    private final ReactiveRedisOperations<String, Coffee> coffeeOps;
    private final ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer;

    public CoffeeReactiveChannel(
            ReactiveRedisOperations<String,
                    Coffee> coffeeOps,
            ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer) {
        this.coffeeOps = coffeeOps;
        this.reactiveRedisMessageListenerContainer = reactiveRedisMessageListenerContainer;
    }

    public Mono<Long> notifyAboutCoffee(Coffee coffee, boolean isDeleted) {
        if (isDeleted) {
            return coffeeOps.convertAndSend(COFFEE_DELETE_TOPIC.getTopic(), coffee);
        }
        return coffeeOps.convertAndSend(COFFEE_TOPIC.getTopic(), coffee);
    }

    public Flux<Coffee> incomingCoffee() {
        ObjectMapper objectMapper = new ObjectMapper();
        return reactiveRedisMessageListenerContainer
                .receive(COFFEE_TOPIC, COFFEE_DELETE_TOPIC)
                .map(ReactiveSubscription.Message::getMessage)
                .onErrorContinue((ex, value) -> log.error(value.toString(), ex))
                .map(m -> {
                    try {
                        return objectMapper.readValue(m, Coffee.class);
                    } catch (IOException e) {
                        log.error("Could not parse raw Coffee: {}", m, e);
                        return new Coffee();
                    }
                }).filter(Objects::nonNull);
    }
}
