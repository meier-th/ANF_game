package com.p3212.tst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"Services", "Repositories", "EntityClasses", "com.p3212.tst"})
@EntityScan("EntityClasses")
@EnableJpaRepositories("Repositories")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
