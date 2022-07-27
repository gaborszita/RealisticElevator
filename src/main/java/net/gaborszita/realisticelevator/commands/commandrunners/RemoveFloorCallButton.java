package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RemoveFloorCallButton implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveFloorCallButton(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 2) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String elevatorName = args[0];
    int floorLevel;
    try {
      floorLevel = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    if (!manager.containsElevator(elevatorName)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + elevatorName + " " +
          "does not exist.");
    } else {
      Elevator elevator = manager.getElevator(elevatorName);
      Elevator.Floor floor = elevator.getFloor(floorLevel);
      if (floor == null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " does " +
            "not exist.");
      } else if (floor.getCallButton() == null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " has no " +
            "call button.");
      } else if (floor.setCallButton(null)) {
        sender.sendMessage("Call button removed from floor " + floorLevel +
            ".");
      } else {
        sender.sendMessage(ChatColor.RED + "Error removing call button from " +
            "floor " + floorLevel + ".\n" +
            "Please check server logs for more information.");
      }
    }
  }

  @Override
  public String getCommand() {
    return "removefloorcallbutton";
  }

  @Override
  public String getDescription() {
    return "Removes a floor call button from an elevator.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number]";
  }

  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n" +
        "[floor number] - Number of the floor to remove call button from";
  }
}