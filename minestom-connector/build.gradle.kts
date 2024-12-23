plugins {
    kotlin("jvm") version "2.0.20"
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:c976f345d1")

    annotationProcessor(project(":impl-loader"))

    api(project(":base")) {
        exclude(group = "mysql", module = "mysql-connector-java")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "io.netty", module = "netty-handler")
        exclude(group = "io.netty", module = "netty-transport-native-epoll")
        exclude(group = "io.netty", module = "netty-codec")
        exclude(group = "com.maxmind.geoip2", module = "geoip2")
        exclude(group = "javatar", module = "javatar")
    }

    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.google.code.gson:gson:2.11.0")

    api(project(":protocol"))
}

tasks {
    shadowJar {
        relocate("org.yaml", "net.skykingdoms.yaml")
        relocate("com.google.gson", "net.skykingdoms.gson")
        val output = rootProject.ext["output"] as String
        destinationDirectory.set(file("$rootDir/$output"))
        archiveFileName.set("BukkitConnector.jar")
    }
}

kotlin {
    jvmToolchain(23)
}
