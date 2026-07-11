package com.clenson.nestbook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.clenson.nestbook.mapper")
@SpringBootApplication
public class NestbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(NestbookApplication.class, args);
    }
}

