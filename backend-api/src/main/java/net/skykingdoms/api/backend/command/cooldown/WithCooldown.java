package net.skykingdoms.api.backend.command.cooldown;

import java.lang.annotation.*;

/**
 * Specifies a cooldown in seconds for command execution
 * @author Vihara
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface WithCooldown {

    int value();

    boolean currentServerOnly() default true;

}
