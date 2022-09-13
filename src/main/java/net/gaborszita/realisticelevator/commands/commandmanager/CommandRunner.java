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

package net.gaborszita.realisticelevator.commands.commandmanager;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * This interface is the base for a command runner, which is responsible for
 * handling its designated command.
 */
public interface CommandRunner {
  /**
   * Message to send if the player needs to run a command.
   */
  String playerNeedsToRunCommandMessage =
      "This command must be run by a player.";

  /**
   * Runs a command.
   *
   * @param sender Command sender.
   * @param args Command arguments.
   */
  void runCommand(@Nonnull CommandSender sender, @Nonnull String[] args);

  /**
   * Returns the command.
   *
   * @return Command.
   */
  @Nonnull
  String getCommand();

  /**
   * Returns the command description.
   *
   * @return Command description.
   */
  @Nonnull
  String getDescription();

  /**
   * Returns the command usage.
   *
   * @return Command usage.
   */
  @Nonnull
  String getUsage();

  /**
   * Returns the command arguments.
   *
   * @return Command arguments.
   */
  @Nonnull
  String getArguments();

  /**
   * Returns the permissions required to run the command.
   *
   * @return Permissions required to run the command.
   */
  @Nonnull
  default String getPermission() {
    return "realisticelevator." + getCommand();
  }

  /**
   * Returns the invalid usage message.
   *
   * @return Invalid usage message.
   */
  @Nonnull
  default String getInvalidUsageMessage() {
    return "Invalid usage. Usage is: " + getUsage();
  }
}
