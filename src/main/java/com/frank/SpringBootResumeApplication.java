package com.frank;

import javafx.application.Application;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@MapperScan(basePackages = "com.frank.dao")
public class SpringBootResumeApplication {

    public static void main(String[] args) {
		SpringApplication.run(SpringBootResumeApplication.class, args);
	}
}
