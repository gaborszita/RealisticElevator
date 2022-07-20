package net.gaborszita.realisticelevator.commands.commandmanager;

import net.gaborszita.realisticelevator.commands.commandmanager.exceptions.CommandNotRegisteredException;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
  private final Map<String, CommandRunner> commands = new HashMap<>();

  /**
   * Registers a new command. If a command has already been registered, it
   * will be assigned the new executor.
   * @param command Command to register
   * @param runner Executor assigned to command
   */
  public void registerCommand(String command, CommandRunner runner) {
    commands.put(command, runner);
  }

  /**
   * Runs a registered command.
   * @param sender Command sender
   * @param command Command to execute
   * @param args Command arguments
   * @return Indicates command success
   * @throws CommandNotRegisteredException If command wasn't registered
   */
  public boolean runCommand(CommandSender sender, String command,
                            String[] args)
                            throws CommandNotRegisteredException {
    CommandRunner runner = commands.get(command);
    if (runner == null) {
      throw new CommandNotRegisteredException("Command not registered: "
          + command);
    }
    return runner.runCommand(sender, args);
  }

  public boolean isCommandRegistered(String command) {
    return commands.containsKey(command);
  }
}
