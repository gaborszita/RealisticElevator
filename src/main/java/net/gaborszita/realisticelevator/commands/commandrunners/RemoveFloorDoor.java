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
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RemoveFloorDoor implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveFloorDoor(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 5) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String elevatorName = args[0];

    int floorNumber;
    try {
      floorNumber = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }

    int[] coords = new int[3];
    for (int i = 2; i < 5; i++) {
      try {
        coords[i - 2] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
    }

    if (!manager.containsElevator(elevatorName)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " +
          elevatorName + " does not exist.");
    } else {
      Elevator elevator =
          Objects.requireNonNull(manager.getElevator(elevatorName));
      Elevator.Floor floor = elevator.getFloor(floorNumber);
      if (floor == null) {
        sender.sendMessage(ChatColor.RED + "Floor " + floorNumber + " does " +
            "not exist.");
      } else if (!floor.containsDoor(coords[0], coords[1], coords[2])) {
        sender.sendMessage(ChatColor.RED + "Door at " + coords[0] + " " +
            coords[1] + " " + coords[2] + " does not exist.");
      } else if (floor.removeDoor(coords[0], coords[1], coords[2])) {
        sender.sendMessage("Door level removed from floor " + floorNumber +
            ".");
      } else {
        sender.sendMessage(ChatColor.RED + "Error removing door level from " +
            "floor.\n" +
            "Please check server logs for more information.");
      }
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "removefloordoor";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Removes a door from a floor.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [floor number] " +
        "[x] [y] [z]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator\n" +
        "[floor number] - Floor number\n" +
        "[x] [y] [z] - Coordinates of the door\n";
  }
}
