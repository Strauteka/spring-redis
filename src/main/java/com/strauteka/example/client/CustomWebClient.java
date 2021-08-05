package com.strauteka.example.client;

import com.strauteka.example.entity.Pong;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.function.Tuple2;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CustomWebClient {
    private final WebClient webClient;
    private final Integer port;
    private final SimpleDateFormat simpleDateFormat;

    public CustomWebClient(Environment environment,
                           @Lazy WebClient webClient,
                           @Lazy SimpleDateFormat simpleDateFormat) {
        this.port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
        this.webClient = webClient;
        this.simpleDateFormat = simpleDateFormat;
    }

    public void getMono() {
        webClient
                .get()
                .uri("/hello/flux/3/250", port)
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve().
                bodyToMono(String.class).subscribe(
                (e) -> log.info("call: {}", e)
        );
    }

    /**
     * Default delimiters new line for string,
     * if missing new line will append in one string
     */
    public void getFlux() {
        webClient.get()
                .uri("/hello/flux/3/250", port)
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(e -> log.info("Next {}", e))
                .subscribe();
    }


    public void getPong() {
        webClient.get().uri(uriBuilder -> uriBuilder
                .path("/ping/buffered/4/200/3")
                .build())
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .bodyToFlux(Pong.class)
                .doOnNext(e -> log.info("Next: {} - {}", e, simpleDateFormat.format(new Date())))
                .onErrorContinue((a, b) -> log.error("{}/{}", a, b))
                .doOnComplete(() -> log.info("End receiving data!"))
                .subscribe(n -> log.info("Result: {} - {}",n, simpleDateFormat.format(new Date())));
    }

    public static Mono<String> callMono(String uri, int port) {
        return getDefaultHttp(uri, port)
                .responseSingle((res, buf) -> buf.asString());
    }

    public static Flux<Tuple2<String, io.netty.handler.codec.http.HttpHeaders>> callFlux(String uri, int port) {
        return getDefaultHttp(uri, port)
                .response((res, buff) -> buff.asString().zipWith(Mono.just(res.responseHeaders()).repeat()));
    }

    private static HttpClient.ResponseReceiver<?> getDefaultHttp(String uri, int port) {
        return HttpClient.create()
                .port(port)
                .wiretap(true)
                .get()
                .uri(uri);
    }

    @SneakyThrows
    @Bean
    public WebClient webClient(Environment environment) {
        int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)))
                .host(InetAddress.getLocalHost().getHostAddress())
                .port(port);
//                .secure(spec -> spec.sslContext(SslContextBuilder.forClient())
//                        .defaultConfiguration(SslProvider.DefaultConfigurationType.TCP)
//                        .handshakeTimeout(Duration.ofSeconds(30))
//                        .closeNotifyFlushTimeout(Duration.ofSeconds(10))
//                        .closeNotifyReadTimeout(Duration.ofSeconds(10)));
//        ObjectMapper mapper = new ObjectMapper();
//        SimpleModule module = new SimpleModule();
//        module.addDeserializer(PongRequest.class, new PongDeserializer());
////        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        mapper.registerModule(module);

//        ExchangeStrategies strategies = ExchangeStrategies
//                .builder()
//                .codecs(clientDefaultCodecsConfigurer -> {
////                    clientDefaultCodecsConfigurer.registerDefaults(false);
//                    clientDefaultCodecsConfigurer
//                            .defaultCodecs()
//                            .jackson2JsonEncoder(
//                                    new Jackson2JsonEncoder(mapper,
//                                            MediaType.APPLICATION_JSON,
//                                            MediaType.APPLICATION_NDJSON));
//                    clientDefaultCodecsConfigurer
//                            .defaultCodecs()
//                            .jackson2JsonDecoder(
//                                    new Jackson2JsonDecoder(mapper,
//                                            MediaType.APPLICATION_JSON,
//                                            MediaType.APPLICATION_NDJSON));
//                }).build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_NDJSON_VALUE,
                        MediaType.APPLICATION_JSON_VALUE)
                //.exchangeStrategies(strategies)
                .build();
    }

    @Bean("defaultDateFormat")
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
    }
}
