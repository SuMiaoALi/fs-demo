package com.colawz;

import link.thingscloud.freeswitch.esl.spring.boot.starter.EnableFreeswitchEslAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author cola
 */

@SpringBootApplication(scanBasePackages = {
        "com.colawz"
})
@EnableFreeswitchEslAutoConfiguration
@Slf4j
public class InboundStart {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(InboundStart.class);
        ConfigurableApplicationContext run = springApplication.run(args);
        log.info("main==> server run on : {}", run.getEnvironment().getProperty("server.port"));
    }

}
