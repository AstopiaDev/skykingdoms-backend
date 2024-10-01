package net.skykingdoms.api.backend.command;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Specifies alias for this command
 * @author Vihara
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(WithMultipleAliases.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithAlias {

    @NotNull String value();

}
