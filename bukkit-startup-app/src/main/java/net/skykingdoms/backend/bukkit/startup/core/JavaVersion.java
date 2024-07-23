package net.skykingdoms.backend.bukkit.startup.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter
public enum JavaVersion {
/*

    GRAALVM_11("/usr/lib/jvm/graal11/bin/java"),
    GRAALVM_17("/usr/lib/jvm/graal17/bin/java"),
    OPENJDK_20("/usr/lib/jvm/java-20-openjdk/bin/java")
 */
    NORMAL_17("java"),
    GRAALVM_11("java"),
    GRAALVM_17("/usr/lib/jvm/temurin-17-jdk-amd64/bin/java"),
    OPENJDK_20("java"),
    OPENJDK_21("/usr/lib/jvm/temurin-21-jdk-amd64/bin/java")

    ;

    String binaryPath;
}
