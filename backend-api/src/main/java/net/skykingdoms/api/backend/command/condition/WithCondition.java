package net.skykingdoms.api.backend.command.condition;

import java.lang.annotation.*;

/**
 * Specifies a condition that should be TRUE for command execution
 * @author Vihara
 */
@Repeatable(WithMultipleConditions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface WithCondition {

    String value();

}
