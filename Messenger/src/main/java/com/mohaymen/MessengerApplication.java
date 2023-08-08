package com.mohaymen;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessengerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessengerApplication.class, args);
        Logger logger = Logger.getLogger(MessengerApplication.class);
        logger.info("Application started.");
    }

}