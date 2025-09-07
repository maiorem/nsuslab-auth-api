plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // jpa
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    annotationProcessor("org.springframework.boot:spring-boot-starter-data-jpa")

    // jdbc-mysql
    runtimeOnly("com.mysql:mysql-connector-j")

    // TestFixtures dependencies for MySQL testcontainer
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
    testFixturesImplementation("org.testcontainers:mysql")
    testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}