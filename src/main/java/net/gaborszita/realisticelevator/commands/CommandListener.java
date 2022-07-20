package net.gaborszita.realisticelevator.commands;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import net.gaborszita.realisticelevator.commands.commandmanager.exceptions.CommandNotRegisteredException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class CommandListener implements CommandExecutor {
  private final CommandManager manager;

  public CommandListener(CommandManager manager) {
    this.manager = manager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command,
                           String label, String[] args) {
    boolean ret;
    if (args.length==0) {
      sender.sendMessage("Usage is: " + command.getUsage());
      ret = true;
    } else {
      String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
      if (manager.isCommandRegistered(args[0])) {
        ret = manager.runCommand(sender, args[0], commandArgs);
      } else {
        sender.sendMessage("No such command: " + args[0]);
        ret = false;
      }
    }
    return ret;
  }
}
