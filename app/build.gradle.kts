plugins {
    application
    distribution
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.rabbitmq:amqp-client:5.16.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
}

application {
    mainClass.set("repro.App")
}

task<Exec>("buildDockerImage") {
    commandLine("docker", "build", "--rm", "-t", "repro:1.0.0", ".")
}

tasks.build { dependsOn("buildDockerImage") }
