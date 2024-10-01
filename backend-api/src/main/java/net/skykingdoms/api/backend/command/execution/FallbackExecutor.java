package net.skykingdoms.api.backend.command.execution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <br>"Запасной" экзекутор команды
 * <br>
 * <br>Выполняется, когда подкоманда не найдена
 * <br>Если такой аннотации нет, то выполняется {@link Executor}
 * <br>
 * <br>См. также: {@link SubcommandExecutor}, {@link FallbackExecutor}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FallbackExecutor {
}
