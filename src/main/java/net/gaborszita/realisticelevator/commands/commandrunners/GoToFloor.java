package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GoToFloor implements CommandRunner {
  ElevatorManager manager;

  public GoToFloor(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 1) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }

    int floor;
    try {
      floor = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }

    if (sender instanceof Player) {
      Player player = (Player) sender;
      Optional<Elevator> elevator = manager.getElevators().values().stream()
          .filter(e -> {
            Location playerLoc = player.getLocation();
            int px = playerLoc.getBlockX();
            int py = playerLoc.getBlockY();
            int pz = playerLoc.getBlockZ();
            Location loc1 = e.getLoc1();
            Location loc2 = e.getLoc2();
            int l1x = loc1.getBlockX();
            int l1y = loc1.getBlockY();
            int l1z = loc1.getBlockZ();
            int l2x = loc2.getBlockX();
            int l2y = loc2.getBlockY();
            int l2z = loc2.getBlockZ();
            return px >= Math.min(l1x, l2x) && px <= Math.max(l1x, l2x) &&
                py >= Math.min(l1y, l2y) && py <= Math.max(l1y, l2y) &&
                pz >= Math.min(l1z, l2z) && pz <= Math.max(l1z, l2z);
          })
          .findAny();

      if (!elevator.isPresent()) {
        sender.sendMessage(ChatColor.RED + "You are not in an elevator.");
      } else if (elevator.get().addStop(floor)) {
        sender.sendMessage("Floor " + floor + " added to elevator " +
            elevator.get().getName() + " stop queue.");
      } else {
        sender.sendMessage(ChatColor.RED + "Floor " + floor + " does not " +
            "exist in elevator " + elevator.get().getName() + ".");
      }
    } else {
      sender.sendMessage(playerNeedsToRunCommandMessage);
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "goto";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Goes to a floor. Player has to be in an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator goto [floor]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[floor] - Floor to go to";
  }
}
