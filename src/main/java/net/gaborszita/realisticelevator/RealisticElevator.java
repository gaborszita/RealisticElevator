package net.gaborszita.realisticelevator;

import net.gaborszita.realisticelevator.commandlistener.CommandListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class RealisticElevator extends JavaPlugin {
  @Override
  public void onEnable() {
    getLogger().info("Loading RealisticElevator");
    Objects.requireNonNull(this.getCommand("elevator"))
        .setExecutor(new CommandListener());
    getLogger().info("RealisticElevator enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info("RealisticElevator disabled");
  }
}
