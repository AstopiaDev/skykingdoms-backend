package net.skykingdoms.api.backend.command.execution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <br>Основной экзекутор команды.
 * <br>
 * <br>Вызывается, когда подкоманда не найдена или
 * <br>массив аргументов пуст
 * <br>
 * <br>См. также: {@link SubcommandExecutor}, {@link FallbackExecutor}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Executor {

}
