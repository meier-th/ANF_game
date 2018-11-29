package com.p3212.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.p3212.Services", "com.p3212.Repositories", "com.p3212.EntityClasses", "com.p3212.main", "com.p3212.configuration"})
@EntityScan("com.p3212.EntityClasses")
@EnableJpaRepositories("com.p3212.Repositories")
public class Application {
     
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        
    }
}
