package net.skykingdoms.api.backend.command;

import java.lang.annotation.*;

/**
 * Executors annotated by this annotation, will be processed asynchronously
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Async {

}
