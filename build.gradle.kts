import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  java
  id("org.springframework.boot") version "4.0.3"
}

group = "com.anf"
version = "4.3"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))

  // Spring Boot starters (Boot 4 renames)
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-websocket")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")

  // Database
  implementation("org.postgresql:postgresql")

  // Lombok
  compileOnly("org.projectlombok:lombok:1.18.42")
  annotationProcessor("org.projectlombok:lombok:1.18.42")

  // Tests
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testCompileOnly("org.projectlombok:lombok:1.18.42")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
}

springBoot {
  mainClass = "com.anf.Application"
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    exceptionFormat = TestExceptionFormat.FULL
  }
}
