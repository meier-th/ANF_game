package com.anf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.anf.model")
@EnableJpaRepositories("com.anf.repository")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
