package com.strauteka.example.service;

import com.strauteka.example.entity.Coffee;
import com.strauteka.example.repository.CoffeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoffeeService {

    public CoffeeService(CoffeeRepository coffeeRepository) {
        this.coffeeRepository = coffeeRepository;
    }

    private final CoffeeRepository coffeeRepository;

    public List<Coffee> all() {
        return coffeeRepository.all();
    }

    public Coffee find(Long id) {
        return this.coffeeRepository.find(id);
    }

    public Long count() {
        return this.coffeeRepository.count();
    }
}
