package com.strauteka.example.repository;

import com.strauteka.example.entity.Coffee;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.strauteka.example.repository.CoffeeReactiveRepository.COFFEE_ALL;
import static com.strauteka.example.repository.CoffeeReactiveRepository.COFFEE_PREFIX;
@Repository
public class CoffeeRepository {


    private final RedisTemplate<String, Coffee> stringCoffeeRedisTemplate;

    public CoffeeRepository(RedisTemplate<String, Coffee> stringCoffeeRedisTemplate) {
        this.stringCoffeeRedisTemplate = stringCoffeeRedisTemplate;
    }

    public Coffee find(Long id) {
        return this.stringCoffeeRedisTemplate.opsForValue().get(COFFEE_PREFIX + id);
    }

    public Long count() {
        return (long) Objects.requireNonNull(this.stringCoffeeRedisTemplate.keys(COFFEE_ALL)).size();
    }

    public List<Coffee> all() {
        return Objects.requireNonNull(this.stringCoffeeRedisTemplate
                .keys(COFFEE_ALL))
                .parallelStream()
                .map(rawCafe -> this.stringCoffeeRedisTemplate.opsForValue().get(rawCafe)).collect(Collectors.toList());
    }
}
