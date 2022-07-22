package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class ListFloors implements CommandRunner {
  private final ElevatorManager manager;

  public ListFloors(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 1) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String elevatorName = args[0];
    if (!manager.containsElevator(elevatorName)) {
      sender.sendMessage(ChatColor.RED + "Elevator " + elevatorName +
          " does not exist");
    } else {
      sender.sendMessage("Elevator " + elevatorName + " has " +
          manager.getElevator(elevatorName).getFloors().size() +
          " floors:\n" + manager.getElevator(elevatorName).getFloors()
          .entrySet().stream().map(v -> "Floor " + v.getKey() + " coords: " +
              v.getValue().getLocation().getBlockX() + " " +
              v.getValue().getLocation().getBlockY() + " " +
              v.getValue().getLocation().getBlockZ())
          .collect(Collectors.joining("\n")));
    }
  }

  @Override
  public String getCommand() {
    return "listfloors";
  }

  @Override
  public String getDescription() {
    return "Lists all floors of an elevator.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name]";
  }

  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator";
  }
}
