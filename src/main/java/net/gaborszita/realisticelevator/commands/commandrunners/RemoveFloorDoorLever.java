package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RemoveFloorDoorLever implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveFloorDoorLever(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
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
      sender.sendMessage(ChatColor.RED + "Elevator with name " +
          elevatorName + " does not exist.");
    } else {
      Elevator elevator =
          Objects.requireNonNull(manager.getElevator(elevatorName));
      Elevator.Floor floor = elevator.getFloor(floorLevel);
      if (floor == null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " does " +
            "not exist.");
      } else if (!floor.containsDoorLever(coords[0], coords[1], coords[2])) {
        sender.sendMessage(ChatColor.RED + "Door lever at " + coords[0] + " " +
            coords[1] + " " + coords[2] + " does not exist.");
      } else if (floor.removeDoorLever(coords[0], coords[1], coords[2])) {
        sender.sendMessage("Door level removed from floor " + floorLevel +
            ".");
      } else {
        sender.sendMessage(ChatColor.RED + "Error removing door level from " +
            "floor.\n" +
            "Please check server logs for more information.");
      }
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "removefloordoorlever";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Removes a door lever from a floor.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number] " +
        "[x] [y] [z]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n" +
        "[floor number] - Floor number\n" +
        "[x] [y] [z] - Coordinates of the door lever\n";
  }
}
