package net.skykingdoms.api.backend.command;

import java.lang.annotation.*;

/**
 * Specifies a relay executor aliases
 * @author Vihara
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RelayExecutorAlias {

    String[] value();

}
