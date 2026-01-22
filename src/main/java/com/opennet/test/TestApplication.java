package com.opennet.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TestApplication {

	 @Value("${spring.application.name:-}")
    String name;

    @RestController
    class HelloController {
        @GetMapping("/")
        String hello() {
            return "Hello " + name + "!";
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

}
