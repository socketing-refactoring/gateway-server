val springCloudVersion = "2024.0.0"

plugins {
    java
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.2"
}

group = "com.jeein"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

spotless {
    java {
        removeUnusedImports()
        importOrder()
        googleJavaFormat().aosp()
    }

    kotlinGradle {
        target("**/*.gradle.kts", "*.gradle.kts")

        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }

    val prettierConfig by extra("$rootDir/.prettierrc.yml")

    format("markdown") {
        target("**/*.md", "*.md")

        prettier().configFile(prettierConfig)
    }

    format("yaml") {
        target("*.yml", "src/main/resources/*.yml")

        prettier().configFile(prettierConfig)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))
    implementation("io.micrometer:micrometer-registry-prometheus:1.15.0-M2")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.5")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
// 	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
// 	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
// 	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("lintCheck") {
    dependsOn("spotlessCheck")

    doLast {
        println("\u001B[32m✔ Lint check completed successfully!\u001B[0m")
    }
}

tasks.register("lintApply") {
    dependsOn("spotlessApply")
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveFileName.set("gateway-server.jar")
}
