package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public class CreateElevator implements CommandRunner {
  private final JavaPlugin plugin;
  private final ElevatorManager manager;

  public CreateElevator(JavaPlugin plugin, ElevatorManager manager) {
    this.plugin = plugin;
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
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
      if (manager.containsElevator(name)) {
        sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
            "already exists.");
      } else {
        Location loc1 = new Location(player.getWorld(), coords[0], coords[1],
            coords[2]);
        Location loc2 = new Location(player.getWorld(), coords[3], coords[4],
            coords[5]);
        if (Elevator.create(plugin, name, manager, loc1, loc2)) {
          sender.sendMessage("Elevator " + name + " created.");
        } else {
          sender.sendMessage(ChatColor.RED + "Error creating elevator " + name
              + ".\n"
              + "Please check server logs for more information.");
        }
      }
    } else {
      sender.sendMessage(playerNeedsToRunCommandMessage);
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "create";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Creates a new elevator in a cuboid area.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator create " + getCommand() + " [name] [x1] [y1] [z1] [x2]" +
        " [y2] [z2]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[name] - Name of the elevator\n"
        + "[x1] [y1] [z1] - Coordinates of the first vertex of the cuboid\n"
        + "[x2] [y2] [z2] - Coordinates of the second vertex of the cuboid";
  }
}
