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

public class DeleteElevator implements CommandRunner {
  private final ElevatorManager manager;

  public DeleteElevator(ElevatorManager manager) {
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
    } else if (manager.deleteElevator(name)) {
      sender.sendMessage("Elevator " + name + " deleted");
    } else {
      sender.sendMessage(ChatColor.RED + "Elevator " + name
          + " could not be deleted.\n"
          + "Please check server logs for more information.");
    }
  }

  @Nonnull
  @Override
  public String getCommand() {
    return "delete";
  }

  @Nonnull
  @Override
  public String getDescription() {
      return "Deletes an elevator.";
  }

  @Nonnull
  @Override
  public String getUsage() {
      return "/elevator " + getCommand() + " [name]";
  }

  @Nonnull
  @Override
  public String getArguments() {
      return "[name] - Name of the elevator to delete";
  }
}
