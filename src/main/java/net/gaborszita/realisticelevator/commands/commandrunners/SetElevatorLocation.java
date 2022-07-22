package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetElevatorLocation implements CommandRunner {
  private final ElevatorManager manager;

  public SetElevatorLocation(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 7) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    int[] coords = new int[6];
    for (int i = 1; i < 7; i++) {
      try {
        coords[i - 1] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
    }
    if (sender instanceof Player) {
      Player player = (Player) sender;
      Location loc1 = new Location(player.getWorld(), coords[0], coords[1],
          coords[2]);
      Location loc2 = new Location(player.getWorld(), coords[3], coords[4],
          coords[5]);
      if (!manager.containsElevator(name)) {
        sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
            "does not exist.");
      } else if (manager.getElevator(name).setLocation(loc1, loc2)) {
        sender.sendMessage("Elevator " + name + " location set.");
      } else {
        sender.sendMessage(ChatColor.RED + "Error setting elevator " + name +
            " location.\n"
            + "Please check server logs for more information.");
      }
    } else {
      sender.sendMessage(userNeedsToRunCommandMessage);
    }
  }

  @Override
  public String getCommand() {
    return "setlocation";
  }

  @Override
  public String getDescription() {
    return "Sets the location of an elevator.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand()  + " [name] [x1] [y1] [z1] [x2] [y2] " +
        "[z2]";
  }

  @Override
  public String getArguments() {
    return "[name] - Name of the elevator\n"
        + "[x1] [y1] [z1] - Coordinates of the first vertex of the cuboid\n"
        + "[x2] [y2] [z2] - Coordinates of the second vertex of the cuboid";
  }
}
