import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  java
  id("org.springframework.boot") version "3.5.11"
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
  // Import the Spring Boot BOM for managed dependency versions
  implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.11"))

  // Spring Boot starters
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // Spring WebSocket / Messaging
  implementation("org.springframework:spring-websocket")
  implementation("org.springframework:spring-messaging")

  // Spring Security OAuth2 client (used for Google OAuth in auth-google-only PR)
  implementation("org.springframework.security:spring-security-oauth2-client")

  // Database
  implementation("org.postgresql:postgresql")

  // Jackson (versions managed by Boot BOM)
  implementation("com.fasterxml.jackson.core:jackson-core")
  implementation("com.fasterxml.jackson.core:jackson-databind")

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
