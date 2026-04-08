package com.help.mp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.help.mp.mapper")
@EnableAsync
public class HelpMpApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpMpApplication.class, args);
    }
}
