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

package net.gaborszita.realisticelevator;

import net.gaborszita.realisticelevator.commands.CommandListener;
import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import net.gaborszita.realisticelevator.commands.commandrunners.*;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main class for the plugin.
 */
public class RealisticElevator extends JavaPlugin {
  /**
   * Called when the plugin is enabled.
   */
  @Override
  public void onEnable() {
    getLogger().info("Loading RealisticElevator");
    CommandManager commandManager = new CommandManager();
    ElevatorManager manager = new ElevatorManager(this);
    commandManager.registerCommand(new AddDoorLever(manager));
    commandManager.registerCommand(new AddFloor(this, manager));
    commandManager.registerCommand(new AddFloorDoorLever(manager));
    commandManager.registerCommand(new CreateElevator(this, manager));
    commandManager.registerCommand(new DeleteElevator(manager));
    commandManager.registerCommand(new ElevatorInfo(manager));
    commandManager.registerCommand(new GetFloorCallButton(manager));
    commandManager.registerCommand(new GoToFloor(manager));
    commandManager.registerCommand(new ListDoorLevers(manager));
    commandManager.registerCommand(new ListElevators(manager));
    commandManager.registerCommand(new ListFloorDoorLevers(manager));
    commandManager.registerCommand(new ListFloors(manager));
    commandManager.registerCommand(new RemoveDoorLever(manager));
    commandManager.registerCommand(new RemoveFloor(manager));
    commandManager.registerCommand(new RemoveFloorCallButton(manager));
    commandManager.registerCommand(new RemoveFloorDoorLever(manager));
    commandManager.registerCommand(new SetElevatorLocation(manager));
    commandManager.registerCommand(new SetFloorCallButton(manager));
    Objects.requireNonNull(this.getCommand("elevator"))
        .setExecutor(new CommandListener(commandManager));

    // Used for generating command documentation in CSV format:
    /*try (FileWriter writer = new FileWriter("elevatordocs.txt")) {
      List<CommandRunner> runners = new ArrayList<>();
      runners.add(new AddDoorLever(manager));
      runners.add(new AddFloor(this, manager));
      runners.add(new AddFloorDoorLever(manager));
      runners.add(new CreateElevator(this, manager));
      runners.add(new DeleteElevator(manager));
      runners.add(new ElevatorInfo(manager));
      runners.add(new GetFloorCallButton(manager));
      runners.add(new GoToFloor(manager));
      runners.add(new ListDoorLevers(manager));
      runners.add(new ListElevators(manager));
      runners.add(new ListFloorDoorLevers(manager));
      runners.add(new ListFloors(manager));
      runners.add(new RemoveDoorLever(manager));
      runners.add(new RemoveFloor(manager));
      runners.add(new RemoveFloorCallButton(manager));
      runners.add(new RemoveFloorDoorLever(manager));
      runners.add(new SetElevatorLocation(manager));
      runners.add(new SetFloorCallButton(manager));
      writer.write("Command,Description,Usage,Arguments,Permission" +
          System.lineSeparator());
      for (CommandRunner runner : runners) {
        writer.write(String.format("/elevator %s,%s,%s,%s,%s%s",
            runner.getCommand(),
            runner.getDescription(),
            runner.getUsage(),
            runner.getArguments().replace("\n", "<br>"),
            runner.getPermission(),
            System.lineSeparator()));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }*/
    getLogger().info("RealisticElevator enabled");
  }

  /**
   * Called when the plugin is disabled.
   */
  @Override
  public void onDisable() {
    getLogger().info("RealisticElevator disabled");
  }
}
