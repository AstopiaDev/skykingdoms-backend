package net.skykingdoms.api.megamind.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PluginDescription {

    private String name;
    private String main;
    private String version;
    private String author;

    private Set<String> depends = new HashSet<>();
    private Set<String> softDepends = new HashSet<>();

    private Path path;
    private String description;

}
