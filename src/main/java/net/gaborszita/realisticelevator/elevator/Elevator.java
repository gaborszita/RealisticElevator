/*
 * Copyright (C) 2022 Gabor Szita
 *
 * This file is part of RealisticElevator.
 *
 * RealisticElevator is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * RealisticElevator is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with RealisticElevator. If not, see <https://www.gnu.org/licenses/>.
 */

package net.gaborszita.realisticelevator.elevator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents an elevator. It is responsible for handling the elevator's
 * movement, fulfilling stop requests, and opening its doors.
 */
public class Elevator {
  /**
   * Plugin instance of the elevator. Used to create tasks and uses the
   * plugin's logger to log messages.
   */
  private final JavaPlugin plugin;

  /**
   * Name of the elevator.
   */
  private final String name;

  /**
   * Manager of the elevator.
   */
  private final ElevatorManager manager;

  /**
   * First location of the elevator area cuboid region.
   */
  private Location loc1;

  /**
   * Second location of the elevator area cuboid region.
   */
  private Location loc2;

  /**
   * List of doors of the elevator.
   */
  private final List<Vector> doors;

  /**
   * Floors of the elevator. The key if the floor number, and the value is
   * the floor object.
   */
  private final Map<Integer, Floor> floors;

  /**
   * Queued stops of the elevators
   */
  private final Set<Integer> stops;

  /**
   * Elevator mover task.
   */
  private BukkitTask task;

  /**
   * Listens to blocks created and destroyed in the elevator area.
   */
  private final BlockEventListener blockEventListener;

  /**
   * Indicates whether the elevator is loaded, meaning that it is actively
   * listening to stop requests and moving the elevator when a stop request
   * is made.
   */
  private boolean loaded;

  /**
   * Indicates if the elevator is currently moving and fulfilling stop
   * requests.
   */
  private boolean active = false;

  /**
   * Master block of the elevator, i.e. the block with lowest x, y, and z
   * coordinates.
   */
  private Location masterBlock;

  /**
   * Contains all blocks of the moving elevator object.
   */
  private final ArrayList<Location> elevatorBlocks = new ArrayList<>();

  /**
   * Indicates floor elevator is currently at.
   */
  private int currentFloor;

  /**
   * Indicates whether the elevator's doors are open.
   */
  private boolean doorsOpen = false;

  /**
   * Coordinate of the moving elevator object.
   */
  private int lowX, highX, lowZ, highZ;

  /**
   * Height of the moving elevator object.
   */
  private int sizeY;

  /**
   * Private constructor, used by the
   * {@link #create(JavaPlugin, String, ElevatorManager, Location, Location)
   * create} method.
   *
   * @param plugin Plugin instance.
   * @param name Name of the elevator.
   * @param manager Elevator manager.
   * @param loc1 First location of the elevator area cuboid region.
   * @param loc2 Second location of the elevator area cuboid region.
   */
  private Elevator(@Nonnull JavaPlugin plugin, @Nonnull String name,
                   @Nonnull ElevatorManager manager,
                   @Nonnull Location loc1, @Nonnull Location loc2) {
    this.plugin = plugin;
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doors = new ArrayList<>();
    this.floors = new HashMap<>();
    stops = new HashSet<>();
    findBlocks(null);
    blockEventListener = new BlockEventListener();
    plugin.getServer().getPluginManager().registerEvents(blockEventListener,
        plugin);
    loaded = true;
  }

  /**
   * Package-private constructor, used by ElevatorManager to create elevators
   * with all data supplied in arguments.
   *
   * @param plugin Plugin instance.
   * @param name Name of the elevator.
   * @param manager Elevator manager.
   * @param loc1 First location of the elevator area cuboid region.
   * @param loc2 Second location of the elevator area cuboid region.
   * @param doors List of doors.
   */
  Elevator(@Nonnull JavaPlugin plugin, @Nonnull String name,
           @Nonnull ElevatorManager manager,
           @Nonnull Location loc1, @Nonnull Location loc2,
           @Nonnull List<Vector> doors) {
    this.plugin = plugin;
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doors = doors;
    this.floors = new HashMap<>();
    stops = new HashSet<>();
    findBlocks(null);
    blockEventListener = new BlockEventListener();
    plugin.getServer().getPluginManager().registerEvents(blockEventListener,
        plugin);
    loaded = true;
  }

  /**
   * Getter for the name of the elevator.
   *
   * @return Name of the elevator.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Setter for elevator location.
   *
   * @param loc1 First location of the elevator area cuboid region.
   * @param loc2 Second location of the elevator area cuboid region.
   * @return True on success, false on failure.
   */
  public boolean setLocation(@Nonnull Location loc1, @Nonnull Location loc2) {
    Location oldLoc1 = this.loc1;
    Location oldLoc2 = this.loc2;
    this.loc1 = loc1.clone();
    this.loc2 = loc2.clone();
    if (save()) {
      reload();
      return true;
    } else {
      this.loc1 = oldLoc1;
      this.loc2 = oldLoc2;
      return false;
    }
  }

  /**
   * Getter for the first location of the elevator area cuboid region.
   *
   * @return First location of the elevator area cuboid region.
   */
  @Nonnull
  public Location getLoc1() {
    return loc1.clone();
  }

  /**
   * Getter for the second location of the elevator area cuboid region.
   *
   * @return Second location of the elevator area cuboid region.
   */
  @Nonnull
  public Location getLoc2() {
    return loc2.clone();
  }

  /**
   * Getter for the doors of the elevator.
   *
   * @return List of the doors of the elevator.
   */
  @Nonnull
  public List<Vector> getDoors() {
    List<Vector> clonedList = new ArrayList<>();
    for (Vector door: doors) {
      clonedList.add(door.clone());
    }
    return clonedList;
  }

  /**
   * Adds a door to the elevator.
   *
   * @param door Coordinate of door relative to the master block.
   * @return True on success, false on failure.
   * @see #masterBlock
   */
  public boolean addDoor(@Nonnull Vector door) {
    doors.add(door.clone());
    if (save()) {
      reload();
      return true;
    } else {
      doors.remove(doors.size() - 1);
      return false;
    }
  }

  /**
   * Checks if the elevator contains a door.
   *
   * @param door Coordinate of door relative to the master block.
   * @return True if the elevator contains the door, false otherwise.
   * @see #masterBlock
   */
  public boolean containsDoor(@Nonnull Vector door) {
    return doors.contains(door);
  }

  /**
   * Removes a door from the elevator.
   *
   * @param door Coordinate of door relative to the master block.
   * @return True on success, false on failure.
   * @see #masterBlock
   */
  public boolean removeDoor(@Nonnull Vector door) {
    if (!doors.remove(door)) {
      return false;
    } else if (save()) {
      reload();
      return true;
    } else {
      doors.add(door.clone());
      return false;
    }
  }

  /**
   * Getter for the floors of the elevator.
   *
   * @return Map of the floors of the elevator.
   * @see #floors
   */
  @Nonnull
  public Map<Integer, Floor> getFloors() {
    return new HashMap<>(floors);
  }

  /**
   * Getter for a floor of the elevator.
   *
   * @param floor Floor number.
   * @return Floor object for the floor.
   */
  @Nullable
  public Floor getFloor(int floor) {
    return floors.get(floor);
  }

  /**
   * Adds a floor to the elevator.
   *
   * @param floor Floor object.
   * @return True on success, false on failure.
   */
  private boolean addFloor(int floorNumber, @Nonnull Floor floor) {
    Floor oldFloor = floors.put(floorNumber, floor);
    if (save()) {
      if (oldFloor != null) {
        oldFloor.unload();
      }
      reload();
      return true;
    } else {
      floors.put(floorNumber, oldFloor);
      return false;
    }
  }

  /**
   * Checks if the elevator contains a floor.
   *
   * @param floorNumber Floor number.
   * @return True if the elevator contains the floor, false otherwise.
   */
  public boolean containsFloor(int floorNumber) {
    return floors.containsKey(floorNumber);
  }

  /**
   * Removes a floor from the elevator.
   *
   * @param floorNumber Floor number.
   * @return True on success, false on failure.
   */
  public boolean removeFloor(int floorNumber) {
    Floor oldFloor = floors.remove(floorNumber);
    if (save()) {
      if (oldFloor != null) {
        oldFloor.unload();
        reload();
      }
      return oldFloor != null;
    } else {
      floors.put(floorNumber, oldFloor);
      return false;
    }
  }

  /**
   * Adds a floor to the elevator without saving it to the config file. Used
   * by
   * {@link Elevator.Floor(JavaPlugin, Elevator, int, Location, List,
   * Location)}.
   *
   * @param floorNumber Floor number.
   * @param floor Floor object.
   */
  private void addFloorNoSave(int floorNumber, @Nonnull Floor floor) {
    floors.put(floorNumber, floor);
  }

  /**
   * Creates a new elevator.
   *
   * @param plugin Plugin instance.
   * @param name Name of the elevator.
   * @param manager Elevator manager.
   * @param loc1 First location of the elevator area cuboid region.
   * @param loc2 Second location of the elevator area cuboid region.
   * @throws IllegalArgumentException If an elevator with the supplied name
   *                                  already exists.
   * @return True on success, false on failure.
   */
  public static boolean create(@Nonnull JavaPlugin plugin,
                               @Nonnull String name,
                               @Nonnull ElevatorManager manager,
                               @Nonnull Location loc1,
                               @Nonnull Location loc2) {
    if (manager.containsElevator(name)) {
      throw new IllegalArgumentException("Elevator already exists: "
          + name);
    }
    return new Elevator(plugin, name, manager, loc1.clone(),
        loc2.clone()).save();
  }

  /**
   * Adds a stop to the elevator.
   *
   * @param floor Floor number.
   * @return True on success, false on failure.
   */
  public boolean addStop(int floor) {
    if (!floors.containsKey(floor) || !loaded) {
      return false;
    }
    stops.add(floor);
    if (!active && masterBlock != null) {
      active = true;
      task = new Mover().runTaskTimer(plugin, Mover.TICK_INTERVAL,
          Mover.TICK_INTERVAL);
    }
    return true;
  }

  /**
   * Getter for the master block of the elevator.
   *
   * @return Master block of the elevator.
   */
  @Nullable
  public Location getMasterBlock() {
    return masterBlock;
  }

  /**
   * Cancels the elevator mover task.
   *
   * @see #task
   */
  private void cancelTask() {
    if (active) {
      task.cancel();
      active = false;
    }
  }

  /**
   * Reloads the elevator. Used when a property of the elevator is changed.
   */
  private void reload() {
    reload(null);
  }

  /**
   * Reloads the elevator. Used when a property of the elevator is changed.
   *
   * @param ignoreBlock Block to ignore when reloading the elevator.
   */
  private void reload(@Nullable Block ignoreBlock) {
    findBlocks(ignoreBlock);
    stops.clear();
    cancelTask();
  }

  /**
   * Unloads an elevator.
   *
   * @see #loaded
   */
  void unload() {
    if (loaded) {
      cancelTask();
      HandlerList.unregisterAll(blockEventListener);
      for (Floor floor : floors.values()) {
        floor.unload();
      }
      loaded = false;
    }
  }

  /**
   * Checks if elevator was properly unloaded. If not, it will unload it and
   * log an error message.
   */
  @Override
  protected void finalize() {
    if (loaded) {
      unload();
      plugin.getLogger().warning("Elevator " + name + " was not unloaded!");
    }
  }

  /**
   * Finds the blocks of the elevator object.
   *
   * @param ignoreBlock Block to ignore when finding the blocks.
   */
  private void findBlocks(@Nullable Block ignoreBlock) {
    int l1x,l1y,l1z,l2x,l2y,l2z;
    l1x = loc1.getBlockX();
    l1y = loc1.getBlockY();
    l1z = loc1.getBlockZ();
    l2x = loc2.getBlockX();
    l2y = loc2.getBlockY();
    l2z = loc2.getBlockZ();
    // reset the blocks list and masterBlock
    elevatorBlocks.clear();
    masterBlock = null;
    for (int x=Math.min(l1x, l2x); x<=Math.max(l1x, l2x); x++) {
      for (int y=Math.min(l1y, l2y); y<=Math.max(l1y, l2y); y++) {
        for (int z=Math.min(l1z, l2z); z<=Math.max(l1z, l2z); z++) {
          Block block = Objects.requireNonNull(loc1.getWorld())
              .getBlockAt(x, y, z);
          if (!block.getType().equals(Material.AIR) && !block.equals(ignoreBlock)) {
            elevatorBlocks.add(block.getLocation());
            // If the master block is not set, set it to the first block that
            // is not null. Since the loops go from the smallest coordinates to
            // largest, the first discovered block that is not air will have
            // the lowest coordinates, which is the master block.
            if (masterBlock == null) {
              masterBlock = block.getLocation();
            }
          }
        }
      }
    }

    lowX = Math.min(l1x, l2x);
    highX = Math.max(l1x, l2x);
    lowZ = Math.min(l1z, l2z);
    highZ = Math.max(l1z, l2z);
    sizeY = Math.max(l1y, l2y) - Math.min(l1y, l2y) + 1;
  }

  /**
   * Saves the elevator to the config file.
   *
   * @return True on success, false on failure.
   */
  private boolean save() {
    return manager.saveElevator(name, this);
  }

  /**
   * This class listens for block placement and block removal events in the
   * elevator's region.
   */
  private class BlockEventListener implements Listener {
    /**
     * Listens for block placement events.
     *
     * @param event Block placement event.
     */
    @EventHandler
    public void onBlockPlaceEvent(@Nonnull BlockPlaceEvent event) {
      if (!event.isCancelled()) {
        handle(event, null);
      }
    }

    /**
     * Listens for block removal events.
     *
     * @param event Block removal event.
     */
    @EventHandler
    public void onBlockBreakEvent(@Nonnull BlockBreakEvent event) {
      if (!event.isCancelled()) {
        handle(event, event.getBlock());
      }
    }

    /**
     * Handles the event.
     *
     * @param event Event to handle.
     * @param ignoreBlock Block to ignore when reloading the elevator.
     */
    public void handle(@Nonnull BlockEvent event, @Nullable Block ignoreBlock) {
      int l1x,l1y,l1z,l2x,l2y,l2z;
      l1x = loc1.getBlockX();
      l1y = loc1.getBlockY();
      l1z = loc1.getBlockZ();
      l2x = loc2.getBlockX();
      l2y = loc2.getBlockY();
      l2z = loc2.getBlockZ();

      int x = event.getBlock().getX();
      int y = event.getBlock().getY();
      int z = event.getBlock().getZ();

      // Reload the elevator if the block which generated the event is in the
      // elevator's region.
      if (x >= Math.min(l1x, l2x) && x <= Math.max(l1x, l2x) &&
          y >= Math.min(l1y, l2y) && y <= Math.max(l1y, l2y) &&
          z >= Math.min(l1z, l2z) && z <= Math.max(l1z, l2z)) {
        reload(ignoreBlock);
      }
    }
  }

  /**
   * Represents a floor of the elevator.
   */
  public static class Floor {
    /**
     * Plugin instance. Used for logging and creating the call button listener.
     */
    private final JavaPlugin plugin;

    /**
     * Elevator the floor is in.
     */
    private final Elevator elevator;

    /**
     * Location of the floor.
     */
    private final Location loc;

    /**
     * Floor number of the elevator.
     */
    private final int floorNumber;

    /**
     * Doors of the floor.
     */
    private final List<Location> doors;

    /**
     * Call button.
     */
    private Location callButton;

    /**
     * Call button listener.
     */
    private CallButtonListener callButtonListener = null;

    /**
     * Indicates if the floor is loaded, meaning that it is listening to the
     * call button.
     */
    private boolean loaded = true;

    /**
     * Private constructor, used by the {@link Floor#create create} method.
     *
     * @param plugin Plugin instance.
     * @param elevator Elevator the floor is in.
     * @param floorNumber Floor number of the elevator.
     * @param loc Location of the floor.
     */
    private Floor(JavaPlugin plugin, Elevator elevator,
                  int floorNumber, Location loc) {
      this.plugin = plugin;
      this.elevator = elevator;
      this.loc = loc;
      this.floorNumber = floorNumber;
      this.doors = new ArrayList<>();
    }

    /**
     * Package-private constructor, used by ElevatorManager to create a new
     * floor without saving it to the config file.
     *
     * @param plugin Plugin instance.
     * @param elevator Elevator the floor is in.
     * @param floorNumber Floor number of the elevator.
     * @param loc Location of the floor.
     * @param doors Doors of the floor.
     * @param callButton Call button of the floor.
     */
    Floor(@Nonnull JavaPlugin plugin, @Nonnull Elevator elevator,
          int floorNumber, @Nonnull Location loc,
          @Nonnull List<Location> doors, @Nullable Location callButton) {
      this.plugin = plugin;
      this.elevator = elevator;
      this.loc = loc;
      this.floorNumber = floorNumber;
      this.doors = doors;
      this.callButton = callButton;
      reload();
      elevator.addFloorNoSave(floorNumber, this);
    }

    /**
     * Reloads the floor. Used when teh call button of the floor is changed.
     */
    private void reload() {
      if (!loaded) {
        return;
      }

      if (callButton != null && callButtonListener == null) {
        callButtonListener = new CallButtonListener();
        plugin.getServer().getPluginManager().registerEvents(
            callButtonListener, plugin);
      } else if (callButton == null && callButtonListener != null) {
        HandlerList.unregisterAll(callButtonListener);
        callButtonListener = null;
      }
    }

    /**
     * Unloads the floor.
     *
     * @see Floor#loaded
     */
    private void unload() {
      if (loaded) {
        if (callButtonListener != null) {
          HandlerList.unregisterAll(callButtonListener);
          callButtonListener = null;
        }
        loaded = false;
      }
    }

    /**
     * Checks if floor was properly unloaded. If not, it will unload it and
     * log an error message.
     */
    @Override
    protected void finalize() {
      if (loaded) {
        unload();
        plugin.getLogger().warning("Floor " + floorNumber + " of elevator "
            + elevator.getName() + " was not unloaded!");
      }
    }

    /**
     * Creates a new floor.
     *
     * @param plugin Plugin instance.
     * @param elevator Elevator the floor is in.
     * @param floorNumber Floor number of the elevator.
     * @param loc Location of the floor.
     * @return True on success, false in failure.
     */
    public static boolean create(@Nonnull JavaPlugin plugin,
                                 @Nonnull Elevator elevator, int floorNumber,
                                 @Nonnull Location loc) {
      return elevator.addFloor(floorNumber, new Floor(plugin, elevator,
          floorNumber, loc.clone()));
    }

    /**
     * Getter for the location of the floor.
     *
     * @return Location of the floor.
     */
    @Nonnull
    public Location getLocation() {
      return loc.clone();
    }

    /**
     * Getter for the doors of the floor.
     *
     * @return List of doors of the floor.
     */
    @Nonnull
    public List<Location> getDoors() {
      List<Location> clonedList = new ArrayList<>();
      for (Location door: doors) {
        clonedList.add(door.clone());
      }
      return clonedList;
    }

    /**
     * Adds a door to the floor.
     *
     * @param loc Location of the door.
     * @return True on success, false in failure.
     */
    public boolean addDoor(@Nonnull Location loc) {
      doors.add(loc.clone());
      if (save()) {
        elevator.reload();
        return true;
      } else {
        doors.remove(doors.size() - 1);
        return false;
      }
    }

    /**
     * Checks if the floor contains a door.
     *
     * @param x X coordinate of the door.
     * @param y Y coordinate of the door.
     * @param z Z coordinate of the door.
     * @return True if the floor contains the door, false otherwise.
     */
    public boolean containsDoor(int x, int y, int z) {
      for (Location door: doors) {
        if (door.getBlockX() == x && door.getBlockY() == y
            && door.getBlockZ() == z) {
          return true;
        }
      }
      return false;
    }

    /**
     * Removes a door from the floor.
     *
     * @param x X coordinate of the door.
     * @param y Y coordinate of the door.
     * @param z Z coordinate of the door.
     * @return True on success, false in failure.
     */
    public boolean removeDoor(int x, int y, int z) {
      for (Location loc: doors) {
        if (loc.getBlockX() == x && loc.getBlockY() == y
            && loc.getBlockZ() == z) {
          doors.remove(loc);
          if (save()) {
            elevator.reload();
            return true;
          } else {
            doors.add(loc);
            return false;
          }
        }
      }
      return false;
    }

    /**
     * Sets the call button for the floor. To delete the call button, pass
     * null as the parameter for the location of the call button.
     *
     * @param callButton Location of the call button.
     * @return True on success, false in failure.
     */
    public boolean setCallButton(@Nullable Location callButton) {
      Location oldCallButton = this.callButton;
      if (callButton != null) {
        this.callButton = callButton.clone();
      } else {
        this.callButton = null;
      }
      if (save()) {
        reload();
        return true;
      } else {
        this.callButton = oldCallButton;
        return false;
      }
    }

    /**
     * Getter for the call button of the floor.
     *
     * @return Location of the call button.
     */
    @Nullable
    public Location getCallButton() {
      return callButton;
    }

    /**
     * Listener for the call button of the floor.
     */
    private class CallButtonListener implements Listener {
      /**
       * Listens for a player interact event. If the player interacts with
       * the call button, it will add the floor to the elevator's stop queue.
       *
       * @param event Player interact event.
       */
      @EventHandler
      public void onPlayerInteractEvent(@Nonnull PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
          Location blockLoc = Objects.requireNonNull(event.getClickedBlock())
              .getLocation();
          if (blockLoc.getBlockX() == callButton.getBlockX() &&
              blockLoc.getBlockY() == callButton.getBlockY() &&
              blockLoc.getBlockZ() == callButton.getBlockZ() &&
              blockLoc.getBlock().getBlockData() instanceof Switch) {
            if (elevator.addStop(floorNumber)) {
              event.getPlayer().sendMessage("Elevator coming to your floor. " +
                  "Please wait.");
            } else {
              event.getPlayer().sendMessage(ChatColor.RED + "Failed to " +
                  "queue elevator to come to your floor.");
            }
          }
        }
      }
    }

    /**
     * Saves the floor to the config file.
     *
     * @return True on success, false in failure.
     */
    public boolean save() {
      return elevator.save();
    }
  }

  /**
   * Moves the elevator object.
   */
  private class Mover extends BukkitRunnable {
    // cannot use enum to represent direction, because Java 8 doesn't allow
    // inner classes to have static members
    /**
     * Direction of the elevator. 1 for up, -1 for down.
     */
    private byte direction = 0;

    /**
     * Delay the mover has to wait for. Used when the elevator is at a floor
     * waiting for players to board.
     */
    private int delay = 0;

    /**
     * Specifies the tick frequency of the mover.
     */
    private static final int TICK_INTERVAL = 10;

    /**
     * Main method that's get called every {@link #TICK_INTERVAL TICK_INTERVAL}
     * ticks.
     */
    @Override
    public void run() {
      if (delay > 0) {
        delay--;
        return;
      }

      // if the elevator is at a stop and has to start moving again, choose
      // its direction
      if (doorsOpen) {
        if (stops.stream().map(floors::get)
            .map(floor -> floor.getLocation().getBlockY())
            .allMatch(y -> direction > 0 ? y <= masterBlock.getBlockY() :
                y >= masterBlock.getBlockY())) {
          direction = (byte)(direction == 1 ? -1 : 1);
        }
      }

      // close doors if open
      setDoorsState(false);

      // if there are no stops, stop the elevator
      if (stops.isEmpty()) {
        cancelTask();
        return;
      }

      // choose direction of the elevator if the mover was just created
      if (direction == 0) {
        direction = -1;
        stops.stream().map(floors::get)
            .map(floor -> floor.getLocation().getBlockY())
            .min(Comparator.comparingInt(y ->
                Math.abs(y - masterBlock.getBlockY())))
            .filter(y -> y > masterBlock.getBlockY())
            .ifPresent(y -> direction = 1);
      }

      // check if at a stop
      if (stops.stream().map(floors::get)
          .anyMatch(floor -> floor.getLocation().getBlockX() ==
              masterBlock.getBlockX() &&
              floor.getLocation().getBlockY() == masterBlock.getBlockY() &&
              floor.getLocation().getBlockZ() == masterBlock.getBlockZ())) {
        // set delay for 5 seconds
        delay = 5 * 20 / TICK_INTERVAL;
        setCurrentFloor();
        // open doors
        setDoorsState(true);
        stops.remove(currentFloor);
        return;
      }

      // move the elevator up or down 1 block
      if (direction == 1) {
        // check if elevator is going out of range
        if (elevatorBlocks.get(elevatorBlocks.size() - 1).getBlockY()+1 >
            Math.max(loc1.getBlockY(), loc2.getBlockY())) {
          plugin.getLogger().warning("Elevator " + name + " was going up " +
              "and reached max height, but still didn't arrive at a stop! " +
              "Please check if floor coordinates are correct.");
          cancelTask();
        } else {
          moveBlocks(1);
        }
      } else {
        // check if elevator is going out of range
        if (elevatorBlocks.get(0).getBlockY()-1 <
            Math.min(loc1.getBlockY(), loc2.getBlockY())) {
          plugin.getLogger().warning("Elevator " + name + " was going down " +
              "and reached min height, but still didn't arrive at a stop! " +
              "Please check if floor coordinates are correct.");
          cancelTask();
        } else {
          moveBlocks(-1);
        }
      }
    }

    /**
     * Moves the elevator blocks by the given number of blocks.
     *
     * @param num Number of blocks to move the elevator by.
     */
    private void moveBlocks(int num) {
      // move players in the elevator
      for (Player p: Objects.requireNonNull(loc1.getWorld()).getPlayers()) {
        if (!p.isFlying() &&
            p.getLocation().getX() >= lowX &&
            p.getLocation().getX() < highX + 1 &&
            p.getLocation().getY() >= masterBlock.getBlockY() &&
            p.getLocation().getY() < masterBlock.getBlockY() + sizeY &&
            p.getLocation().getZ() >= lowZ &&
            p.getLocation().getZ() < highZ + 1) {
          p.teleport(p.getLocation().add(0, num, 0));
        }
      }
      // handle blocks (door and doors) that break when moved as other blocks
      // separately
      List<Integer> breakingBlocksIndex = new ArrayList<>();
      List<BlockData> breakingBlocks = new ArrayList<>();
      // put breaking blocks to the lists
      for (int i=0; i< elevatorBlocks.size(); i++) {
        Material material =
            elevatorBlocks.get(i).getBlock().getBlockData().getMaterial();
        if (material == Material.IRON_DOOR) {
          breakingBlocksIndex.add(i);
          breakingBlocks.add(elevatorBlocks.get(i).getBlock().getBlockData());
        }
      }
      // set the content of these blocks to air and update them to point to
      // the new blocks
      for (Integer index : breakingBlocksIndex) {
        elevatorBlocks.get(index).getBlock().setType(Material.AIR);
        elevatorBlocks.get(index).add(0, num, 0);
      }
      // move normal blocks
      if (num > 0) {
        for (int i= elevatorBlocks.size()-1; i>=0; i--) {
          if (!breakingBlocksIndex.contains(i)) {
            moveBlock(i, num);
            elevatorBlocks.get(i).add(0, num, 0);
          }
        }
      } else if (num < 0) {
        for (int i=0; i< elevatorBlocks.size(); i++) {
          if (!breakingBlocksIndex.contains(i)) {
            moveBlock(i, num);
            elevatorBlocks.get(i).add(0, num, 0);
          }
        }
      }
      // move breaking blocks
      for (int i=0; i<breakingBlocks.size(); i++) {
        elevatorBlocks.get(breakingBlocksIndex.get(i)).getBlock().setBlockData
         (breakingBlocks.get(i));
      }
      // update the master block's coordinates
      masterBlock.add(0, num, 0);
    }

    /**
     * Moves a block by the given number of blocks.
     *
     * @param i Index of the block to move in
     *          {@link #elevatorBlocks elevatorBlocks}.
     * @param num Number of blocks to move the block by.
     */
    private void moveBlock(int i, int num) {
      new Location(elevatorBlocks.get(i).getWorld(),
          elevatorBlocks.get(i).getX(),
          elevatorBlocks.get(i).getY() + num,
          elevatorBlocks.get(i).getZ()).getBlock().setBlockData(
          elevatorBlocks.get(i).getBlock().getBlockData());
      elevatorBlocks.get(i).getBlock().setType(Material.AIR);
    }

    /**
     * Sets the current floor. Used when elevator is at a stop.
     *
     * @throws RuntimeException When unable to find the current floor.
     */
    private void setCurrentFloor() {
      OptionalInt currentFloorOpt = floors.entrySet().stream().filter(
              floor -> floor.getValue().getLocation().getBlockY() ==
                  masterBlock.getBlockY())
          .mapToInt(Map.Entry::getKey).findFirst();
      if (currentFloorOpt.isPresent()) {
        currentFloor = currentFloorOpt.getAsInt();
      } else {
        throw new RuntimeException("Cannot find current floor!");
      }
    }

    /**
     * Opens and closes the doors of the elevator.
     *
     * @param open Whether the door should be open.
     */
    private void setDoorsState(boolean open) {
      // if the doors state is the same as the desired state, do nothing
      if (open == doorsOpen) {
        return;
      }

      Floor floor = floors.get(currentFloor);
      // log a warning message if the floor doors are not of type iron
      // door
      if (floor.getDoors().stream().map(door ->
              door.getBlock().getBlockData().getMaterial())
          .anyMatch(material -> material != Material.IRON_DOOR)) {
        plugin.getLogger().warning("One of floor " + currentFloor + " of " +
            "elevator " + name + "'s doors material type not iron door!");
      }
      // log a warning message if the elevator doors are not of type
      // iron door
      if (doors.stream().map(door -> masterBlock.clone()
              .add(door)).map(Location::getBlock)
          .anyMatch(block -> block.getBlockData().getMaterial() !=
              Material.IRON_DOOR)) {
        plugin.getLogger().warning("One of elevator " + name + "'s doors " +
            "material type not iron door!");
      }
      // set the floor doors state
      floor.getDoors().stream()
          .map(Location::getBlock)
          .filter(block -> block.getBlockData().getMaterial() ==
              Material.IRON_DOOR)
          .forEach(block -> {
            Openable openable = (Openable) block.getBlockData();
            openable.setOpen(open);
            block.setBlockData(openable);
          });
      // set the elevator doors state
      doors.stream().map(door -> masterBlock.clone()
              .add(door)).map(Location::getBlock)
          .filter(block -> block.getBlockData().getMaterial() ==
              Material.IRON_DOOR)
          .forEach(block -> {
            Openable openable = (Openable) block.getBlockData();
            openable.setOpen(open);
            block.setBlockData(openable);
          });
      // update doors state
      doorsOpen = open;
    }
  }
}
