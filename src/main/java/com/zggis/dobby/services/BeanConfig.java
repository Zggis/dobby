package com.zggis.dobby.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

	@Value("${linux}")
	private boolean isLinux;

	@Bean
	public DoviProcessBuilder doviProcessBuilder() {
		if (isLinux) {
			return new DoviLinuxProcessBuilderImpl();
		} else {
			return new DoviWindowsProcessBuilderImpl();
		}
	}

	@Bean
	public MediaService mediaService() {
		if (isLinux) {
			return new LinuxMediaServiceImpl();
		} else {
			return new WindowMediaServiceImpl();
		}
	}
}
