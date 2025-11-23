package com.lgames.gamebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lgames.gamebackend", "com.lgames.gamebackend.snake"})
public class GameBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameBackendApplication.class, args);
    }

}