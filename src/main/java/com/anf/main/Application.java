package com.anf.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.anf.Services", "com.anf.Repositories", "com.anf.EntityClasses", "com.anf.main", "com.anf.Configurations"})
@EntityScan("com.anf.EntityClasses")
@EnableJpaRepositories("com.anf.Repositories")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

    }
}
