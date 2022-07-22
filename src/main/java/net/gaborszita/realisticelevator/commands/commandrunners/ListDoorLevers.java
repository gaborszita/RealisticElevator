package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class ListDoorLevers implements CommandRunner {
  private final ElevatorManager manager;

  public ListDoorLevers(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 1) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator " + name + " does not " +
          "exist");
    } else {
      sender.sendMessage("Door levers of elevator " + name + ":\n" +
          manager.getElevator(name).getDoorLevers().stream()
              .map(v -> v.getBlockX() + " " + v.getBlockY() + " "
                  + v.getBlockZ())
              .collect(Collectors.joining("\n")));
    }
  }

  @Override
  public String getCommand() {
    return "listdoorlevers";
  }

  @Override
  public String getDescription() {
    return "Lists all door levers for an elevator.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [name]";
  }

  @Override
  public String getArguments() {
    return "[name] - the name of the elevator";
  }
}
