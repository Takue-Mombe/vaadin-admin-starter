package com.adminpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.spring.annotation.EnableVaadin;

@SpringBootApplication
@EnableVaadin("com.adminpro.ui")
public class AdminProApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminProApplication.class, args);
    }
}
