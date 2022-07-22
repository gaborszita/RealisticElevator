package net.gaborszita.realisticelevator.commands.commandmanager;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
  private final Map<String, CommandRunner> commands = new HashMap<>();

  /**
   * Registers a new command. If a command has already been registered, it
   * will be assigned the new runner.
   * @param runner Executor assigned to command
   */
  public void registerCommand(CommandRunner runner) {
    commands.put(runner.getCommand(), runner);
  }

  /**
   * Runs a registered command.
   * @param sender Command sender
   * @param command Command to execute
   * @param args Command arguments
   */
  public void runCommand(CommandSender sender, String command,
                            String[] args) {
    CommandRunner runner = commands.get(command);
    if (runner == null) {
      throw new IllegalArgumentException("Command not registered: "
          + command);
    }
    runner.runCommand(sender, args);
  }


  public boolean isCommandRegistered(String command) {
    return commands.containsKey(command);
  }

  public String[] getRegisteredCommands() {
    return commands.keySet().toArray(new String[0]);
  }
}
