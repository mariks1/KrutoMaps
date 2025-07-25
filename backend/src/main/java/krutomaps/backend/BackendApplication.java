package krutomaps.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now());
        SpringApplication.run(BackendApplication.class, args);
    }

}
