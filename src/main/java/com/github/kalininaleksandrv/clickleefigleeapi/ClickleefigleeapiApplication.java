package com.github.kalininaleksandrv.clickleefigleeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClickleefigleeapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClickleefigleeapiApplication.class, args);
	}

}
