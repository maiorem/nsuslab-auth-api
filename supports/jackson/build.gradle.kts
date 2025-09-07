plugins {
    `java-library`
}

dependencies {
    // spring boot
    api("org.springframework.boot:spring-boot-starter-web")
    
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-core") 
    api("com.fasterxml.jackson.core:jackson-annotations")
}