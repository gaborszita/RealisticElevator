package net.gaborszita.realisticelevator.commands.commandrunners;

import net.gaborszita.realisticelevator.commands.commandmanager.CommandRunner;
import net.gaborszita.realisticelevator.elevator.Elevator;
import net.gaborszita.realisticelevator.elevator.ElevatorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ElevatorInfo implements CommandRunner {
    private final ElevatorManager manager;

    public ElevatorInfo(ElevatorManager manager) {
        this.manager = manager;
    }

    @Override
    public void runCommand(@Nonnull CommandSender sender,
                           @Nonnull String[] args) {
        if (args.length != 1) {
            sender.sendMessage(getInvalidUsageMessage());
            return;
        }
        String name = args[0];
        if (!manager.containsElevator(name)) {
          sender.sendMessage(ChatColor.RED + "Elevator " + name +
              " does not exist");
        } else {
          Elevator elevator = Objects.requireNonNull(manager
              .getElevator(name));
          sender.sendMessage("Elevator " + name + " coordinates:\n" +
              elevator.getLoc1().getBlockX() + " " +
              elevator.getLoc1().getBlockY() + " " +
              elevator.getLoc1().getBlockZ() + " " +
              elevator.getLoc2().getBlockX() + " " +
              elevator.getLoc2().getBlockY() + " " +
              elevator.getLoc2().getBlockZ());
        }
    }

    @Nonnull
    @Override
    public String getCommand() {
        return "info";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Lists coordinates of an elevator.";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "/elevator " + getCommand() + " [elevator name]";
    }

    @Nonnull
    @Override
    public String getArguments() {
        return "[elevator name] - the name of the elevator";
    }
}
