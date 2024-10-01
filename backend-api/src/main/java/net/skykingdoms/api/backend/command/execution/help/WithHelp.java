package net.skykingdoms.api.backend.command.execution.help;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <br>
 * <br>Если у команды будет эта аннотация, то
 * <br>в команду добавится подкоманда help.
 * <br>
 * <br>Если <b>primary == true</b>, то основной
 * <br>экзекутор станет Fallback и помощь встанет на его
 * <br>место
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithHelp {

    /**
     * Помощь в основном экзекуторе
     */
    boolean primary() default true;

}
