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

public class AddDoorLever implements CommandRunner {
  private final ElevatorManager manager;

  public AddDoorLever(ElevatorManager manager) {
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
    Vector loc = new Vector(coords[0], coords[1], coords[2]);
    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
          "does not exist.");
    } else if (Objects.requireNonNull(manager.getElevator(name))
        .containsDoorLever(loc)) {
      sender.sendMessage(ChatColor.RED + "Door lever at " +
          loc.getBlockX() + " " +
          loc.getBlockY() + " " +
          loc.getBlockZ() + " " +
          "already exists.");
    } else if (Objects.requireNonNull(manager.getElevator(name))
        .addDoorLever(loc)) {
      sender.sendMessage("Door lever added to elevator " + name + ".");
    } else {
      sender.sendMessage(ChatColor.RED + "Error adding door lever to "
          + "elevator " + name + ".\n"
          + "Please check server logs for more information.");
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "adddoorlever";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Adds a door lever to an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand() + " [elevator name] [x] [y] [z]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[elevator name] - Name of the elevator to add the door lever to.\n"
        + "[x] [y] [z] - Coordinates of the lever relative to the elevator's "
        + "master block (block of the elevator whose coordinates are the "
        + "smallest).";
  }
}
