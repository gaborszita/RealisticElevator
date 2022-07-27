package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListDoorLevers implements CommandRunner {
  private final ElevatorManager manager;

  public ListDoorLevers(ElevatorManager manager) {
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
      sender.sendMessage(ChatColor.RED + "Elevator " + name + " does not " +
          "exist");
    } else {
      sender.sendMessage("Door levers of elevator " + name + ":\n" +
          Objects.requireNonNull(manager.getElevator(name))
              .getDoorLevers().stream()
              .map(v -> v.getBlockX() + " " + v.getBlockY() + " "
                  + v.getBlockZ())
              .collect(Collectors.joining("\n")));
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "listdoorlevers";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Lists all door levers for an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [name]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[name] - the name of the elevator";
  }
}
