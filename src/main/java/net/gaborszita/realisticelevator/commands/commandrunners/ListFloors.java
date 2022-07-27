package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListFloors implements CommandRunner {
  private final ElevatorManager manager;

  public ListFloors(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 1) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String elevatorName = args[0];
    if (!manager.containsElevator(elevatorName)) {
      sender.sendMessage(ChatColor.RED + "Elevator " + elevatorName +
          " does not exist");
    } else {
      Elevator elevator =
          Objects.requireNonNull(manager.getElevator(elevatorName));
      sender.sendMessage("Elevator " + elevatorName + " has " +
          elevator.getFloors().size() +
          " floors:\n" + elevator.getFloors()
          .entrySet().stream().map(v -> "Floor " + v.getKey() + " coords: " +
              v.getValue().getLocation().getBlockX() + " " +
              v.getValue().getLocation().getBlockY() + " " +
              v.getValue().getLocation().getBlockZ())
          .collect(Collectors.joining("\n")));
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "listfloors";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Lists all floors of an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator";
  }
}
