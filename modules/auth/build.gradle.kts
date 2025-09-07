plugins {
    id("java-library")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

tasks.bootJar {
    enabled = false
}

dependencies {
    api(project(":modules:jpa"))
    api(project(":modules:redis"))
    
    // Spring Security & BCrypt
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.security:spring-security-crypto")
    
    // JWT
    api("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}