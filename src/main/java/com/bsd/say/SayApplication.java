package com.bsd.say;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bsd.say.mapper")
public class SayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SayApplication.class, args);
    }

}
