package com.rangiffler;

import com.rangiffler.service.cors.PropertiesLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RangifflerGatewayApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(RangifflerGatewayApplication.class);
        springApplication.addListeners(new PropertiesLogger());
        springApplication.run(args);
    }

}
