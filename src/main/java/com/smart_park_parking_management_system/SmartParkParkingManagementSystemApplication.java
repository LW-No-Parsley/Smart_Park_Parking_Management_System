package com.smart_park_parking_management_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baomidou.mybatisplus.samples.quickstart.mapper")
public class SmartParkParkingManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartParkParkingManagementSystemApplication.class, args);
	}

}
