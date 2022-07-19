package net.gaborszita.realisticelevator;

import net.gaborszita.realisticelevator.commandlistener.CommandListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RealisticElevator extends JavaPlugin {
  @Override
  public void onEnable() {
    getLogger().info("RealisticElevator enabled");
  }

  @Override
  public void onDisable() {
    getLogger().info("RealisticElevator disabled");
  }
}
