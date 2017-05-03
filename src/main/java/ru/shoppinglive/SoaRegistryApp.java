package ru.shoppinglive;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import ru.shoppinglive.commons.core.EnvCheck;

/**
 * Created by rkhabibullin on 04.04.2017.
 */
@SpringBootApplication
@EnableEurekaServer
public class SoaRegistryApp {
    public static void main(String[] args){
        new SpringApplicationBuilder(SoaRegistryApp.class).bannerMode(Banner.Mode.OFF)
                .profiles(EnvCheck.getProfile()).run(args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
