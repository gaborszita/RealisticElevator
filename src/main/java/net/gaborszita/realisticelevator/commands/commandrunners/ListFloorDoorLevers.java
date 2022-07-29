package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListFloorDoorLevers implements CommandRunner {
  private final ElevatorManager manager;

  public ListFloorDoorLevers(ElevatorManager manager) {
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
    int floor;
    try {
      floor = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator " + name +
          " does not " + "exist");
    } else if (Objects.requireNonNull(manager.getElevator(name))
        .getFloor(floor) == null) {
      sender.sendMessage(ChatColor.RED + "Floor " + floor + " does not " +
          "exist");
    } else {
      sender.sendMessage("Door levers of elevator " + name +
          " floor " + floor + " :\n" +
          Objects.requireNonNull(
              Objects.requireNonNull(manager.getElevator(name))
                  .getFloor(floor))
          .getDoorLevers().stream()
          .map(v -> v.getBlockX() + " " + v.getBlockY() + " "
              + v.getBlockZ())
          .collect(Collectors.joining("\n")));
    }
  }


  @Nonnull
  @Override
  public String getCommand() {
    return "listfloordoorlevers";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Lists door levers for an elevator floor.";
  }

    @Nonnull
    @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number]";
  }

    @Nonnull
    @Override
  public String getArguments() {
    return "[elevator name] - the name of the elevator\n" +
        "[floor number] - Floor number";
  }
}
