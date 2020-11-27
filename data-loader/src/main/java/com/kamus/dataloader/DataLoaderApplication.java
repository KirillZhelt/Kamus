package com.kamus.dataloader;

import com.kamus.dataloader.runner.PollLoaderRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DataLoaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataLoaderApplication.class, args);
	}

	@Bean
	CommandLineRunner run(PollLoaderRunner pollRunner) {
		return (args) -> pollRunner.poll();
	}

}
