package com.sehoprojectmanagerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableCaching(proxyTargetClass = true)
@EnableJpaAuditing
@SpringBootApplication
public class SehoprojectManagerApiApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(SehoprojectManagerApiApplication.class, args);
    }
}
