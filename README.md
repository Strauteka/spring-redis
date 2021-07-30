# Spring WebFlux, Redis, Swagger

Example of Spring WebFlux and Redis Reactive API  
Data flow through Redis PUB/SUB channel on successful data alter.

Requires 
1) Java 11.0.9 +
2) Apache Maven 3.6.3 +  
3) Redis version 6.0.10 + 
## Run
opt 1  
`mvn spring-boot:run`  
opt 2  
`mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8080'`  
opt 3  
`mvn clean install`  
`spring-redis>java -jar -Dserver.port=8080 .\target\spring-redis-0.0.1-SNAPSHOT.jar`

## Redis
`docker run --name=local-redis -p=127.0.0.1:6379:6379 --rm redis:alpine`

## Swagger API documentation
http://127.0.0.1:8080/swagger-ui/index.html

SSE  
`curl -N --http2 -H "Accept:text/event-stream" http://127.0.0.1:8080/coffee/sse`


## Usage

1) Start two instances of Spring Service
    1) `mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8080'`
    2) `mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'`
2) Connect and Receive SSE
    1) `curl -N --http2 -H "Accept:text/event-stream" http://127.0.0.1:8080/coffee/sse`
   
3) Edit data on second service API
    1) http://127.0.0.1:8081/swagger-ui/index.html
    
_You should receive notifications on SSE connection through Redis PUB/SUB_