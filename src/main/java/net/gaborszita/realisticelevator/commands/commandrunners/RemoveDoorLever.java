package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RemoveDoorLever implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveDoorLever(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 4) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    int[] coords = new int[3];
    for (int i = 1; i < 4; i++) {
      try {
        coords[i - 1] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
    }
    Vector vector = new Vector(coords[0], coords[1], coords[2]);
    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name
          + " does not exist.");
    } else if (!Objects.requireNonNull(manager.getElevator(name))
        .containsDoorLever(vector)) {
      sender.sendMessage(ChatColor.RED + "Door lever does not exist on " +
          "elevator " + name + " .");
    } else if (Objects.requireNonNull(manager.getElevator(name))
        .removeDoorLever(vector)) {
      sender.sendMessage("Door lever removed from floor " + name + ".");
    } else {
      sender.sendMessage(ChatColor.RED + "Error removing door lever from "
          + "elevator " + name + ".\n"
          + "Please check server logs for more information.");
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "removedoorlever";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Removes a door lever from an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [name] [x] [y] [z]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[name] - Name of the elevator to remove the door lever from.\n"
        + "[x] [y] [z] - Coordinates of the lever relative to the elevator's "
        + "master block (block of the elevator whose coordinates are the "
        + "smallest).";
  }
}
