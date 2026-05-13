package com.example.spring_batch_practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SpringBatchPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchPracticeApplication.class, args);
	}

}
