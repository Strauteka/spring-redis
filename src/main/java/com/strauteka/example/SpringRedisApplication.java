package com.strauteka.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class SpringRedisApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringRedisApplication.class, args);
	}
}
