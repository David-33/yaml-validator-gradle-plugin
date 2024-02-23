plugins {
    kotlin("jvm") version "1.9.22"

    id("java-gradle-plugin")
    id("maven-publish")
}

group = "org.yaml.validator.plugin"
version = "0.1.0"

gradlePlugin {
    plugins {
        create("yaml-validator-plugin") {
            id = "$group.yaml-validator-plugin"
            implementationClass = "$group.YamlValidatorPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}
