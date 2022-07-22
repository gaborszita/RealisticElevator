package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AddFloor implements CommandRunner {
  private final JavaPlugin plugin;
  private final ElevatorManager manager;

  public AddFloor(JavaPlugin plugin, ElevatorManager manager) {
    this.plugin = plugin;
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
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

    if (sender instanceof Player) {
      Player player = (Player) sender;
      Location loc = new Location(player.getWorld(), coords[0], coords[1],
          coords[2]);

      if (!manager.containsElevator(name)) {
        sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
            "does not exist.");
      } else {
        Elevator elevator = manager.getElevator(name);
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
    } else {
      sender.sendMessage(userNeedsToRunCommandMessage);
    }
  }

  @Override
  public String getCommand() {
    return "addfloor";
  }

  @Override
  public String getDescription() {
    return "Adds a floor to an elevator.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number] [x]" +
        " [y] [z]";
  }

  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n"
        + "[floor number] - Floor number/level\n"
        + "[x] [y] [z] - Coordinates of the lever relative to the elevator's "
        + "master block (block of the elevator whose coordinates are the "
        + "smallest).";
  }
}
