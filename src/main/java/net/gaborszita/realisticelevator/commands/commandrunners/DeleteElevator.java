package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class DeleteElevator implements CommandRunner {
  private final ElevatorManager manager;

  public DeleteElevator(ElevatorManager manager) {
      this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 1) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator " + name + " does not " +
          "exist");
    } else if (manager.deleteElevator(name)) {
      sender.sendMessage("Elevator " + name + " deleted");
    } else {
      sender.sendMessage(ChatColor.RED + "Elevator " + name
          + " could not be deleted.\n"
          + "Please check server logs for more information.");
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "delete";
  }

  @Nonnull
  @Override
  public String getDescription() {
      return "Deletes an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
      return "/elevator " + getCommand() + " [name]";
  }

  @Nonnull
  @Override
  public String getArguments() {
      return "[name] - Name of the elevator to delete";
  }
}
