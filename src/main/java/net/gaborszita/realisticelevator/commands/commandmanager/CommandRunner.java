package net.gaborszita.realisticelevator.commands.commandmanager;

import org.bukkit.command.CommandSender;

public interface CommandRunner {
  boolean runCommand(CommandSender sender, String[] args);
  String getDescription();
  String getUsage();
  String getArguments();
}
