package com.frank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@MapperScan(basePackages = "com.frank.dao")
@ServletComponentScan
public class SpringBootResumeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootResumeApplication.class, args);
	}
}
