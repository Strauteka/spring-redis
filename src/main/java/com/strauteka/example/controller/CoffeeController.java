package com.strauteka.example.controller;

import com.strauteka.example.entity.Coffee;
import com.strauteka.example.service.CoffeeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("coffee")
public class CoffeeController {

    private final CoffeeService coffeeService;

    public CoffeeController(CoffeeService coffeeService) {
        this.coffeeService = coffeeService;
    }

    @GetMapping()
    List<Coffee> all() {
        return this.coffeeService.all();
    }

    @GetMapping("{id}")
    Coffee find(@PathVariable("id") Long id) {
        return this.coffeeService.find(id);
    }

    @GetMapping("count")
    Long count() {
        return this.coffeeService.count();
    }
}
