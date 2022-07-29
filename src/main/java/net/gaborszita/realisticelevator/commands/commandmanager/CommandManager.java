package net.gaborszita.realisticelevator.commands.commandmanager;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages commands. This class is responsible for calling te associated
 * command runner for each command.
 */
public class CommandManager {
  private final Map<String, CommandRunner> commands = new HashMap<>();

  /**
   * Registers a new command. If a command has already been registered, it
   * will be assigned the new runner.
   *
   * @param runner Runner assigned to command.
   */
  public void registerCommand(@Nonnull CommandRunner runner) {
    commands.put(runner.getCommand(), runner);
  }

  /**
   * Runs a registered command.
   *
   * @param sender Command sender.
   * @param command Command to execute.
   * @param args Command arguments.
   * @throws IllegalArgumentException If command is not registered.
   */
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String command, @Nonnull String[] args) {
    CommandRunner runner = commands.get(command);
    if (runner == null) {
      throw new IllegalArgumentException("Command not registered: "
          + command);
    }
    runner.runCommand(sender, args);
  }

  /**
   * Checks if a command is registered.
   *
   * @param command Command to check.
   * @return True if command is registered, false otherwise.
   */
  public boolean isCommandRegistered(@Nonnull String command) {
    return commands.containsKey(command);
  }

  /**
   * Returns a list of registered commands.
   *
   * @return List of registered commands.
   */
  @Nonnull
  public String[] getRegisteredCommands() {
    return commands.keySet().toArray(new String[0]);
  }

  /**
   * Returns the command runner for a command.
   *
   * @param command Command.
   * @return Command runner for the command or null if command is not
   *         registered.
   */
  @Nullable
  public CommandRunner getCommandRunner(@Nonnull String command) {
    return commands.get(command);
  }
}
