package net.gaborszita.realisticelevator.commandlistener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandListener implements CommandExecutor {
  int l1x = -62;
  int l1y = 108;
  int l1z = -12;
  int l2x = -60;
  int l2y = 108;
  int l2z = -10;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
                           String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;
      sender.sendMessage("Working");

      // move across x axis

      ArrayList<Block> blocksToMove = new ArrayList<>();

      // get blocks to move
      for (int x=Math.max(l1x, l2x); x>=Math.min(l1x, l2x); x--) {
        for (int y=Math.min(l1y, l2y); y<=Math.max(l1y, l2y); y++) {
          for (int z=Math.min(l1z, l2z); z<=Math.max(l1z, l2z); z++) {
            blocksToMove.add(new Location(player.getWorld(), x, y, z)
                .getBlock());
          }
        }
      }

      for (Block block: blocksToMove) {
        new Location(player.getWorld(), block.getX()+1, block.getY(),
            block.getZ()).getBlock().setType(block.getType());
        if (block.getX() == Math.min(l1x, l2x)) {
          block.setType(Material.AIR);
        }
      }

      l1x++;
      l2x++;

      return true;
    } else {
      sender.sendMessage("This command must be run by a player.");
      return false;
    }
  }
}
