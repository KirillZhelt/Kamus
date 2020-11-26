package com.kamus.dataloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataLoaderApplication {

	private static final Logger logger = LoggerFactory.getLogger(DataLoaderApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DataLoaderApplication.class, args);
	}

}
