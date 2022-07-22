package net.gaborszita.realisticelevator.commands;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandListener implements CommandExecutor {
  private final CommandManager manager;

  public CommandListener(CommandManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command,
                           String label, String[] args) {
    if (args.length==0) {
      String commands = "[" + String.join("|",
          manager.getRegisteredCommands()) + "]";

      sender.sendMessage("Usage is: /elevator " + commands);
    } else {
      String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
      if (manager.isCommandRegistered(args[0])) {
        manager.runCommand(sender, args[0], commandArgs);
      } else {
        sender.sendMessage("No such command: " + args[0]);
      }
    }
    return true;
  }
}
