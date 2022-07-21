package net.gaborszita.realisticelevator.commands.commandmanager;

import org.bukkit.command.CommandSender;

public interface CommandRunner {
  String userNeedsToRunCommandMessage = "This command must be run by a player.";
  void runCommand(CommandSender sender, String[] args);
  String getCommand();
  String getDescription();
  String getUsage();
  String getArguments();
  default String getInvalidUsageMessage() {
    return "Invalid usage. Usage is: " + getUsage();
  }
}
