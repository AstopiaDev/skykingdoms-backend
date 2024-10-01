package net.skykingdoms.api.backend.command.localization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <br>Указать ключ локализации
 * <br>
 * <br>По умолчанию - command_<имя команды>_<имя подкоманды(если есть)>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithLocalizationKey {

    String value();

}
