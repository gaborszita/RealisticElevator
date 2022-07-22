package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ListElevators implements CommandRunner {
  private final ElevatorManager manager;

  public ListElevators(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(CommandSender sender, String[] args) {
    if (args.length != 0) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    Map<String, Elevator> elevators = manager.getElevators();
    sender.sendMessage("Elevators on server: " + String.join(", ",
        elevators.keySet()));
  }

  @Override
  public String getCommand() {
    return "list";
  }

  @Override
  public String getDescription() {
    return "Lists all elevators on this server.";
  }

  @Override
  public String getUsage() {
    return "/elevator " + getCommand();
  }

  @Override
  public String getArguments() {
    return "";
  }
}
