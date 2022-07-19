package net.gaborszita.realisticelevator.commands;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import net.gaborszita.realisticelevator.commands.commandmanager.exceptions.CommandNotRegisteredException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.logging.Logger;

public class CommandListener implements CommandExecutor {
  private final CommandManager manager;
  private final Logger logger;

  public CommandListener(CommandManager manager, Logger logger) {
    this.manager = manager;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command,
                           String label, String[] args) {
    String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
    boolean ret;
    try {
      ret = manager.runCommand(sender, args[0], commandArgs);
    } catch (CommandNotRegisteredException e) {
      logger.warning("Attempted to run command that wasn't registered: "
          + args[0]);
      ret = false;
    }
    return ret;
  }
}
