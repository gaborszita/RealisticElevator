package net.gaborszita.realisticelevator;

import net.gaborszita.realisticelevator.commands.CommandListener;
import net.gaborszita.realisticelevator.commands.commandmanager.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class RealisticElevator extends JavaPlugin {
  @Override
  public void onEnable() {
    getLogger().info("Loading RealisticElevator");
    CommandManager commandManager = new CommandManager();
    Objects.requireNonNull(this.getCommand("elevator"))
        .setExecutor(new CommandListener(commandManager, getLogger()));
    getLogger().info("RealisticElevator enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info("RealisticElevator disabled");
  }
}
