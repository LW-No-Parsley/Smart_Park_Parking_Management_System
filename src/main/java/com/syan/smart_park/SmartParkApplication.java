package com.syan.smart_park;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.syan.smart_park.dao")
@EnableScheduling
@EnableRetry
public class SmartParkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartParkApplication.class, args);
	}

}
