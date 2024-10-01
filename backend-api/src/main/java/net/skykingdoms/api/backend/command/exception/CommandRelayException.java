package net.skykingdoms.api.backend.command.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <br>Редиректит игрока на другую команду.
 * <br>
 * <br>Допустим игрок пишет команду /is visit <его ник>
 * <br>его редиректит на /is home
 * <br>
 * <br>Если игрок напишет /party chat, то его
 * <br>редиректит на /pchat
 */
@Getter
@RequiredArgsConstructor
public class CommandRelayException extends CommandException {

    private final String relay;

}
