package net.skykingdoms.api.backend.command;

import java.lang.annotation.*;

/**
 * Primary command annotation, that specifies main name of the command
 * @author Vihara
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NamedCommand {

    String value();

}
