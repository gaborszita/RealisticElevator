package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.Objects;

public class AddFloor implements CommandRunner {
  private final JavaPlugin plugin;
  private final ElevatorManager manager;

  public AddFloor(JavaPlugin plugin, ElevatorManager manager) {
    this.plugin = plugin;
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 5) {
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
    int[] coords = new int[3];
    for (int i = 2; i < 5; i++) {
      try {
        coords[i - 2] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
    }



    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
          "does not exist.");
    } else {
      Elevator elevator = Objects.requireNonNull(manager.getElevator(name));
      Location loc = new Location(elevator.getLoc1().getWorld(), coords[0],
          coords[1], coords[2]);
      if (elevator.getFloor(floorLevel) != null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " already " +
            "exists.");
      } else if (Elevator.Floor.create(plugin, elevator, floorLevel, loc)) {
        sender.sendMessage("Floor " + floorLevel + " added to elevator " +
            name + ".");
      } else {
        sender.sendMessage(ChatColor.RED + "Error adding floor " +
            " to elevator " + name + ".\n"
            + "Please check server logs for more information.");
      }
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "addfloor";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Adds a floor to an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number] [x]" +
        " [y] [z]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n"
        + "[floor number] - Floor number/level\n"
        + "[x] [y] [z] - Coordinates of the lever relative to the elevator's "
        + "master block (block of the elevator whose coordinates are the "
        + "smallest).";
  }
}
