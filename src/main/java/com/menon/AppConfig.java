package com.menon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig
{
    @Autowired
    EurekaDiscoveryClient discoveryClient;

    @Bean(name = "customer-service-validate")
    public WebClient webClientAuthService(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .baseUrl("http://localhost:8085/customer/v1/validate")
                .filter(new LoggingWebClientFilter())
                .build();
    }

}
