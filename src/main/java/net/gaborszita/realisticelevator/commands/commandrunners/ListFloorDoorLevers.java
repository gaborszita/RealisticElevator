/**
 * Copyright (C) 2022 Gabor Szita
 *
 * This file is part of RealisticElevator.
 *
 * RealisticElevator is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * RealisticElevator is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with RealisticElevator. If not, see <https://www.gnu.org/licenses/>.
 */

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
