plugins {
    java
    kotlin("jvm") version "1.5.0"
}

subprojects {
    group = "md.photocloud-microservice"
    repositories {
        mavenCentral()
        jcenter()
        maven("https://repository.jboss.org/nexus/content/repositories/public/")
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}