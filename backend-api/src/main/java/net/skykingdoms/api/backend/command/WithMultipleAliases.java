package net.skykingdoms.api.backend.command;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Specifies multiple aliases for the command
 * @author Vihara
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithMultipleAliases {

    WithAlias @NotNull [] value();

}
