package net.gaborszita.realisticelevator.testcommandlistener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class CommandListener extends BukkitRunnable implements CommandExecutor {
  int l1x = -62;
  int l1y = 108;
  int l1z = -12;
  int l2x = -60;
  int l2y = 108;
  int l2z = -10;
  World world;
  boolean active = false;
  BukkitTask task;
  JavaPlugin plugin;

  public CommandListener(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    // move across x axis

    List<Block> blocksToMove = new ArrayList<>();

    // get blocks to move
    for (int x=Math.max(l1x, l2x); x>=Math.min(l1x, l2x); x--) {
      for (int y=Math.min(l1y, l2y); y<=Math.max(l1y, l2y); y++) {
        for (int z=Math.min(l1z, l2z); z<=Math.max(l1z, l2z); z++) {
          blocksToMove.add(new Location(world, x, y, z)
              .getBlock());
        }
      }
    }

    for (Block block: blocksToMove) {
      new Location(world, block.getX()+1, block.getY(),
          block.getZ()).getBlock().setType(block.getType());
      if (block.getX() == Math.min(l1x, l2x)) {
        block.setType(Material.AIR);
      }
    }

    // move players who are in elevator
    for (Player p: world.getPlayers()) {
      if (!p.isFlying() &&
          p.getLocation().getX() >= Math.min(l1x, l2x) &&
          p.getLocation().getX() < Math.max(l1x, l2x) + 1 &&
          p.getLocation().getY() >= Math.min(l1y, l2y) &&
          p.getLocation().getY() <= Math.max(l1y, l2y)+3 &&
          p.getLocation().getZ() >= Math.min(l1z, l2z) &&
          p.getLocation().getZ() < Math.max(l1z, l2z) + 1) {
        p.teleport(p.getLocation().add(1, 0, 0));
      }
    }

    l1x++;
    l2x++;
  }


  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
                           String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;
      sender.sendMessage("Working");


      if (!active) {
        world = player.getWorld();
        active = true;
        task = this.runTaskTimer(plugin, 10, 10);
      } else {
        active = false;
        task.cancel();
      }

      return true;
    } else {
      sender.sendMessage("This command must be run by a player.");
      return false;
    }
  }
}
