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
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RemoveDoor implements CommandRunner {
  private final ElevatorManager manager;

  public RemoveDoor(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 4) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    int[] coords = new int[3];
    for (int i = 1; i < 4; i++) {
      try {
        coords[i - 1] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
    }
    Vector vector = new Vector(coords[0], coords[1], coords[2]);
    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name
          + " does not exist.");
    } else if (!Objects.requireNonNull(manager.getElevator(name))
        .containsDoor(vector)) {
      sender.sendMessage(ChatColor.RED + "Door does not exist on " +
          "elevator " + name + " .");
    } else if (Objects.requireNonNull(manager.getElevator(name))
        .removeDoor(vector)) {
      sender.sendMessage("Door removed from floor " + name + ".");
    } else {
      sender.sendMessage(ChatColor.RED + "Error removing door from "
          + "elevator " + name + ".\n"
          + "Please check server logs for more information.");
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "removedoor";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Removes a door from an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [x] [y] [z]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator to remove the door " +
        "from.\n"
        + "[x] [y] [z] - Coordinates of the door relative to the elevator's "
        + "master block (block of the elevator whose coordinates are the "
        + "smallest).";
  }
}
