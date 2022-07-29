package net.gaborszita.realisticelevator.commands;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

/**
 * Main command listener. Implements the Bukkit CommandExecutor interface.
 */
public class CommandListener implements CommandExecutor {
  /**
   * Command manager.
   */
  private final CommandManager manager;

  /**
   * Constructor.
   *
   * @param manager Command manager.
   */
  public CommandListener(@Nonnull CommandManager manager) {
    this.manager = manager;
  }

  /**
   * Executes a command's associated command runner.
   *
   * @param sender Command sender.
   * @param command Command.
   * @param label Command label.
   * @param args Command arguments.
   * @return True
   */
  @Override
  public boolean onCommand(@Nonnull CommandSender sender,
                           @Nonnull Command command,
                           @Nonnull String label, String[] args) {
    if (args.length==0) {
      String commands = "[" + String.join("|",
          manager.getRegisteredCommands()) + "]";

      sender.sendMessage("Usage is: /elevator " + commands);
    } else {
      String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
      if (manager.isCommandRegistered(args[0])) {
        if (sender.hasPermission(Objects.requireNonNull(manager
            .getCommandRunner(args[0])).getPermission())) {
          manager.runCommand(sender, args[0], commandArgs);
        } else {
          sender.sendMessage(ChatColor.RED + "You don't have permission to " +
              "use this command.");
        }
      } else {
        sender.sendMessage("No such command: " + args[0]);
      }
    }
    return true;
  }
}
