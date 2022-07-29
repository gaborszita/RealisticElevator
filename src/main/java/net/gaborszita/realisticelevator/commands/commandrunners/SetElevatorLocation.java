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
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SetElevatorLocation implements CommandRunner {
  private final ElevatorManager manager;

  public SetElevatorLocation(ElevatorManager manager) {
    this.manager = manager;
  }

  @Override
  public void runCommand(@Nonnull CommandSender sender,
                         @Nonnull String[] args) {
    if (args.length != 7) {
      sender.sendMessage(getInvalidUsageMessage());
      return;
    }
    String name = args[0];
    int[] coords = new int[6];
    for (int i = 1; i < 7; i++) {
      try {
        coords[i - 1] = Integer.parseInt(args[i]);
      } catch (NumberFormatException e) {
        sender.sendMessage(getInvalidUsageMessage());
        return;
      }
    }

    if (!manager.containsElevator(name)) {
      sender.sendMessage(ChatColor.RED + "Elevator with name " + name + " " +
          "does not exist.");
    } else {
      Elevator elevator = Objects.requireNonNull(manager.getElevator(name));
      Location loc1 = new Location(elevator.getLoc1().getWorld(), coords[0],
          coords[1], coords[2]);
      Location loc2 = new Location(elevator.getLoc1().getWorld(), coords[3],
          coords[4], coords[5]);
      if (elevator.setLocation(loc1, loc2)) {
        sender.sendMessage("Elevator " + name + " location set.");
      } else {
        sender.sendMessage(ChatColor.RED + "Error setting elevator " + name +
            " location.\n"
            + "Please check server logs for more information.");
      }
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "setlocation";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Sets the location of an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
    return "/elevator " + getCommand()  + " [name] [x1] [y1] [z1] [x2] [y2] " +
        "[z2]";
  }

  @Nonnull
  @Override
  public String getArguments() {
    return "[name] - Name of the elevator\n"
        + "[x1] [y1] [z1] - Coordinates of the first vertex of the cuboid\n"
        + "[x2] [y2] [z2] - Coordinates of the second vertex of the cuboid";
  }
}
