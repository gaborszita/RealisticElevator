package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Map;

public class ListElevators implements CommandRunner {
  private final ElevatorManager manager;

  public ListElevators(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 0) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    Map<String, Elevator> elevators = manager.getElevators();
    sender.sendMessage("Elevators on server: " + String.join(", ",
        elevators.keySet()));
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "list";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Lists all elevators on this server.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand();
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "";
  }
}
