package com.zggis.dobby.services;


import com.zggis.dobby.batch.ConsoleColor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    private static final Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @Value("${linux}")
    private boolean isLinux;

    @Autowired
    private BuildProperties buildProperties;

    @PostConstruct
    public void init() {
        logger.info(ConsoleColor.CYAN.value + "Running Dobby v{}" + ConsoleColor.NONE.value, buildProperties.getVersion());
    }

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
