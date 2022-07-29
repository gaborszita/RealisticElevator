package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RemoveFloorCallButton implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveFloorCallButton(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 2) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String elevatorName = args[0];
    int floorNumber;
    try {
      floorNumber = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    if (!manager.containsElevator(elevatorName)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + elevatorName + " " +
          "does not exist.");
    } else {
      Elevator elevator =
          Objects.requireNonNull(manager.getElevator(elevatorName));
      Elevator.Floor floor = elevator.getFloor(floorNumber);
      if (floor == null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorNumber + " does " +
            "not exist.");
      } else if (floor.getCallButton() == null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorNumber + " has no " +
            "call button.");
      } else if (floor.setCallButton(null)) {
        sender.sendMessage("Call button removed from floor " + floorNumber +
            ".");
      } else {
        sender.sendMessage(ChatColor.RED + "Error removing call button from " +
            "floor " + floorNumber + ".\n" +
            "Please check server logs for more information.");
      }
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "removefloorcallbutton";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Removes a floor call button from an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n" +
        "[floor number] - Number of the floor to remove call button from";
  }
}
