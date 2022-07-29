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
