package com.wisrc.autoetl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.wisrc.excel",
                "com.wisrc.webapp"
        }
)
public class AutoetlApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoetlApplication.class, args);
    }
}
