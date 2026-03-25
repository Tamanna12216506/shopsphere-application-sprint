package com.capgemini.catlogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CatlogserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatlogserviceApplication.class, args);
	}

}
