package net.skykingdoms.api.backend.command.condition;

import java.lang.annotation.*;

/**
 * Annotation that is used in ConditionProvider manager to define
 * a processor for specified condition name.
 *
 * @author Vihara
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ConditionProvider {

    /**
     * Condition name
     */
    String value();

    // Priority <= 0 is reserved for system
    // do not use values lower than or eq 0
    // please
    int priority() default 1;

    boolean showInHelp() default true;

}
