package com.zggis.dobby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DobyApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(DobyApplication.class, args)));
	}

}
