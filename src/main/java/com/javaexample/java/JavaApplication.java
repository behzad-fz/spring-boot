package com.javaexample.java;

import com.javaexample.java.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(RsaKeyProperties.class)
public class JavaApplication {

	@RequestMapping("/")
	String home() {
		return "Hello JAVA World!";
	}

	public static void main(String[] args) {
		SpringApplication.run(JavaApplication.class, args);
	}

}
