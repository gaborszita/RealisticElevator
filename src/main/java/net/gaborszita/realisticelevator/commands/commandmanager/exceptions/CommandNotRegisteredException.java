package net.gaborszita.realisticelevator.commands.commandmanager.exceptions;

public class CommandNotRegisteredException extends IllegalArgumentException {
  public CommandNotRegisteredException(String message) {
    super(message);
  }
}
