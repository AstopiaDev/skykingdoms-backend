package net.skykingdoms.backend.base.dependency;

import java.lang.annotation.*;

/**
 * Fields annotated by this annotation are being processed in {@link DependencyInjection DI}
 * @author Vihara
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Dependency {
}
