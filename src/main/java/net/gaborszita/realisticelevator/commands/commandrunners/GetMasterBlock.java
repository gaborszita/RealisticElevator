package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GetMasterBlock implements CommandRunner {
  private final ElevatorManager manager;

  public GetMasterBlock(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 1) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }

    String name = args[0];
    if (!manager.containsElevator(name)) {
      sender.sendMessage("Elevator " + name + " does not exist");
    } else {
      Location masterBlock = Objects.requireNonNull(manager
          .getElevator(name)).getMasterBlock();
      sender.sendMessage("Elevator " + name + " master block: " +
          masterBlock.getBlockX() + " " +
          masterBlock.getBlockY() + " " +
          masterBlock.getBlockZ());
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "getmasterblock";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Gets the master block of an elevator.";
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
