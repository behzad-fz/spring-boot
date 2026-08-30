package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableTransactionManagement
@EnableScheduling
public class JavaApplication {

	@RequestMapping("/")
	String home() {
		return "Hello JAVA World!";
	}

	public static void main(String[] args) {
		SpringApplication.run(JavaApplication.class, args);
	}

}
