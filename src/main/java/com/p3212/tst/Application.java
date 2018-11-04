package com.p3212.tst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication//(scanBasePackageClasses = {Services.UserService.class, Repositories.UserRepository.class, EntityClasses.User.class})
@ComponentScan(basePackages = {"Services", "Repositories", "EntityClasses"})
@EnableJpaRepositories("Repositories")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        
    }
}
