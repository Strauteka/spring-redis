package com.strauteka.example.scheduler;

import com.strauteka.example.client.CustomWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Objects;


@Slf4j
@Component
public class ScheduledTasks {
    private final CustomWebClient customWebClient;
    private final Integer port;

    public ScheduledTasks(Environment environment, CustomWebClient customWebClient) {
        this.customWebClient = customWebClient;
        this.port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));
    }

    @Scheduled(fixedRate = 10000 )
    private void requestAlive() {
        customWebClient.getPong();
    }

    private void requestStaticAlive() {
        CustomWebClient.callMono("/hello/flux/3/250", this.port)
                .doOnSubscribe(
                        (e) -> log.info("Mono Execute started: {}", e.toString())
                )
                .doOnError(
                        (e) -> log.error("Mono someError", e)
                )
                .doOnNext(
                        next -> log.info("Mono Receive {} ", next)
                ).doFinally(sign -> log.info("Mono Execute Call Ended {} ", sign))
                .subscribe();

        CustomWebClient.callFlux("/hello/flux/3/250",this.port)
                .doOnSubscribe(
                        (e) -> log.info("Execute started: {}", e.toString())
                )
                .doOnError(
                        (e) -> log.error("someError", e)
                )
                .doOnNext(
                        next -> log.info("Receive {} ", next.toString())
                ).doFinally(sign -> log.info("Execute Call Ended {} ", sign))
                .subscribe();
    }
}
