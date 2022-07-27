package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GetFloorCallButton implements CommandRunner {
  private final ElevatorManager manager;

  public GetFloorCallButton(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 2) {
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

    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
          "does not exist.");
    } else {
      Elevator elevator = Objects.requireNonNull(manager.getElevator(name));
      if (!elevator.containsFloor(floorLevel)) {
      sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " does not " +
          "exist.");
      } else if (Objects.requireNonNull(elevator.getFloor(floorLevel))
          .getCallButton() == null) {
        sender.sendMessage("Floor " + floorLevel + " has no call " +
            "button.");
      } else {
        Location loc =
            Objects.requireNonNull(elevator.getFloor(floorLevel))
                .getCallButton();
        sender.sendMessage("Floor " + floorLevel + " call button: " +
            Objects.requireNonNull(loc)
                .getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
      }
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "getfloorcallbutton";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Gets the location of the floor call button for a floor.";
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
        "[floor number] - Number of the floor";
  }
}
