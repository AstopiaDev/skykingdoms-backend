package net.skykingdoms.api.backend.command;

import java.lang.annotation.*;

/**
 * Specifies a command with primary name (#value) pointing to this method
 * @author Vihara
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RelayExecutor {

    String value();

}
