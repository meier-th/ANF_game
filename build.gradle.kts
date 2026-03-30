import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  java
  id("org.springframework.boot") version "4.0.3"
  id("com.google.protobuf") version "0.9.5"
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
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("com.google.protobuf:protobuf-java:4.31.1")

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

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:4.31.1"
  }
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

tasks.register("printJvmArgs") {
    doLast {
        println("JVM Arguments: ${System.getProperty("java.vm.info")}")
        println("Spring Profiles Active: ${System.getProperty("spring.profiles.active")}")
        println("JAVA_TOOL_OPTIONS: ${System.getenv("JAVA_TOOL_OPTIONS")}")
        println("JAVA_OPTS: ${System.getenv("JAVA_OPTS")}")
    }
}