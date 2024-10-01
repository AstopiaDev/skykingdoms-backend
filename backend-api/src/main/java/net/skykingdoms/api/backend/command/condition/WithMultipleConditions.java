package net.skykingdoms.api.backend.command.condition;

import java.lang.annotation.*;

/**
 * @see WithCondition
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface WithMultipleConditions {

    WithCondition[] value();

}
