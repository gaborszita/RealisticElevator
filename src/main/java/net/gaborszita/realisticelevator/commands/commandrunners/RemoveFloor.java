/*
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

public class RemoveFloor implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveFloor(ElevatorManager manager) {
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
    int floorNumber;
    try {
      floorNumber = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }

    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
          "does not exist.");
    } else if (!Objects.requireNonNull(manager.getElevator(name))
        .containsFloor(floorNumber)) {
      sender.sendMessage(ChatColor.RED + "Floor " + floorNumber + " does not " +
          "exist.");
    } else if (Objects.requireNonNull(manager.getElevator(name))
        .removeFloor(floorNumber)) {
      sender.sendMessage("Floor " + floorNumber + " removed from elevator " +
          name + ".");
    } else {
      sender.sendMessage(ChatColor.RED + "Error removing floor " + floorNumber +
          " from elevator " + name + ".\n"
          + "Please check server logs for more information.");
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "removefloor";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Removes a floor from an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n"
        + "[floor number] - Number of the floor to remove";
  }
}
