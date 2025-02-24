package com.rangiffler;

import com.rangiffler.service.PropertiesLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RangifflerAuthApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(RangifflerAuthApplication.class);
        springApplication.addListeners(new PropertiesLogger());
        springApplication.run(args);
    }
}
