package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class GetFloorCallButton implements CommandRunner {
  private final ElevatorManager manager;

  public GetFloorCallButton(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
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
    } else if (!manager.getElevator(name).containsFloor(floorLevel)) {
      sender.sendMessage(ChatColor.RED + "Floor " + floorLevel + " does not " +
          "exist.");
    } else if (manager.getElevator(name).getFloor(floorLevel)
        .getCallButton() == null) {
      sender.sendMessage("Floor " + floorLevel + " has no call " +
          "button.");
    } else {
      Location loc =
          manager.getElevator(name).getFloor(floorLevel).getCallButton();
      sender.sendMessage("Floor " + floorLevel + " call button: " +
          loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
    }
  }

  @Override
  public String getCommand() {
    return "getfloorcallbutton";
  }

  @Override
  public String getDescription() {
    return "Gets the location of the floor call button for a floor.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number]";
  }

  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n" +
        "[floor number] - Number of the floor";
  }
}
