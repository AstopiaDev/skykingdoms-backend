package net.skykingdoms.api.backend.command.execution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <br>Указать метод, который будет обрабатывать
 * <br>сессию перед её выполнением.
 * <br>
 * <br>В параметрах метода обязательно должен быть
 * <br>класс {@link ExecutionSession} или любой другой, который
 * <br>наследует {@link ExecutionSession}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrepareExecution {

}
