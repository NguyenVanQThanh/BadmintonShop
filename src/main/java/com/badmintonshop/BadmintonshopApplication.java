package com.badmintonshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.badmintonshop.repository")
@ComponentScan(basePackages = "com.badmintonshop")
public class BadmintonshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadmintonshopApplication.class, args);
	}

}
