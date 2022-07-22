package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RemoveFloor implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveFloor(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 2) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    int floorLevel;
    try {
      floorLevel = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }

    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
          "does not exist.");
    } else if (!manager.getElevator(name).containsFloor(floorLevel)) {
      sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " does not " +
          "exist.");
    } else if (manager.getElevator(name).removeFloor(floorLevel)) {
      sender.sendMessage("Floor " + floorLevel + " removed from elevator " +
          name + ".");
    } else {
      sender.sendMessage(ChatColor.RED + "Error removing floor " + floorLevel +
          " from elevator " + name + ".\n"
          + "Please check server logs for more information.");
    }
  }

  @Override
  public String getCommand() {
    return "removefloor";
  }

  @Override
  public String getDescription() {
    return "Removes a floor from an elevator.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number]";
  }

  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n"
        + "[floor number] - Number/level of the floor to remove";
  }
}
