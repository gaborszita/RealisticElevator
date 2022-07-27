package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class SetFloorCallButton implements CommandRunner {
  private final ElevatorManager manager;

  public SetFloorCallButton(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 5) {
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

    int[] coords = new int[3];
    for (int i = 2; i < 5; i++) {
      try {
        coords[i - 2] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
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
      } else if (floor.setCallButton(new Location(
          elevator.getLoc1().getWorld(), coords[0], coords[1], coords[2]))) {
        sender.sendMessage("Call button added to floor " + floorLevel + ".");
      } else {
        sender.sendMessage(ChatColor.RED + "Error adding call button to " +
            "floor " + floorLevel + ".\n" +
            "Please check server logs for more information.");
      }
    }
  }

  @Override
  public String getCommand() {
    return "setfloorcallbutton";
  }

  @Override
  public String getDescription() {
    return "Sets the call button for a floor.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number] " +
        "[x] [y] [z]";
  }

  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n" +
        "[floor number] - Number of the floor to add call button to\n" +
        "[x] [y] [z] - Coordinates of the call button";
  }
}