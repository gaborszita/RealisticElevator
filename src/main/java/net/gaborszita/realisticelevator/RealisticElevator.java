package net.gaborszita.realisticelevator;

import net.gaborszita.realisticelevator.commands.CommandListener;
import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import net.gaborszita.realisticelevator.commands.commandrunners.*;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class RealisticElevator extends JavaPlugin {
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
    commandManager.registerCommand(new ListDoorLevers(manager));
    commandManager.registerCommand(new ListElevators(manager));
    commandManager.registerCommand(new ListFloorDoorLevers(manager));
    commandManager.registerCommand(new ListFloors(manager));
    commandManager.registerCommand(new RemoveDoorLever(manager));
    commandManager.registerCommand(new RemoveFloor(manager));
    commandManager.registerCommand(new RemoveFloorDoorLever(manager));
    commandManager.registerCommand(new SetElevatorLocation(manager));
    Objects.requireNonNull(this.getCommand("elevator"))
        .setExecutor(new CommandListener(commandManager));
    getLogger().info("RealisticElevator enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info("RealisticElevator disabled");
  }
}
