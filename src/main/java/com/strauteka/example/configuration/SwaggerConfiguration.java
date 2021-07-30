package com.strauteka.example.configuration;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

@Configuration
@EnableSwagger2
//@EnableSwagger2WebFlux
//http://localhost:8080/swagger-ui/index.html
public class SwaggerConfiguration {

    @Bean
    Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .genericModelSubstitutes(Mono.class, Flux.class, Publisher.class)
                .ignoredParameterTypes(ServerWebExchange.class)

                .select()
                .apis(RequestHandlerSelectors.basePackage("com.strauteka.example.controller"))
                .build();
    }
}
