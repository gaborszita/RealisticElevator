package net.gaborszita.realisticelevator.elevator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Elevator {
  private final JavaPlugin plugin;
  private final String name;
  private final ElevatorManager manager;
  private Location loc1;
  private Location loc2;
  private final List<Vector> doorLevers;
  private final Map<Integer, Floor> floors;
  private final Set<Integer> stops;
  private BukkitTask task;
  private final BlockEventListener blockEventListener;
  private boolean loaded;
  private boolean active = false;
  private Location masterBlock;
  private final ArrayList<Location> elevatorBlocks = new ArrayList<>();
  private int currentFloor = 0;
  private boolean doorsOpen = false;
  private int lowX, highX, lowZ, highZ, sizeY;

  private Elevator(JavaPlugin plugin, String name, ElevatorManager manager,
                   Location loc1, Location loc2) {
    this.plugin = plugin;
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doorLevers = new ArrayList<>();
    this.floors = new HashMap<>();
    stops = new HashSet<>();
    findBlocks();
    blockEventListener = new BlockEventListener();
    plugin.getServer().getPluginManager().registerEvents(blockEventListener,
        plugin);
    loaded = true;
  }

  Elevator(JavaPlugin plugin, String name, ElevatorManager manager,
           Location loc1, Location loc2,
           List<Vector> doorLevers) {
    this.plugin = plugin;
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doorLevers = doorLevers;
    this.floors = new HashMap<>();
    stops = new HashSet<>();
    findBlocks();
    blockEventListener = new BlockEventListener();
    plugin.getServer().getPluginManager().registerEvents(blockEventListener,
        plugin);
    loaded = true;
  }

  public String getName() {
    return name;
  }

  public boolean setLocation(Location loc1, Location loc2) {
    Location oldLoc1 = this.loc1;
    Location oldLoc2 = this.loc2;
    this.loc1 = loc1;
    this.loc2 = loc2;
    if (save()) {
      reload();
      return true;
    } else {
      this.loc1 = oldLoc1;
      this.loc2 = oldLoc2;
      return false;
    }
  }

  public Location getLoc1() {
    return loc1;
  }

  public Location getLoc2() {
    return loc2;
  }

  public List<Vector> getDoorLevers() {
    List<Vector> clonedList = new ArrayList<>();
    for (Vector lever: doorLevers) {
      clonedList.add(lever.clone());
    }
    return clonedList;
  }

  public boolean addDoorLever(Vector lever) {
    doorLevers.add(lever);
    if (save()) {
      reload();
      return true;
    } else {
      doorLevers.remove(doorLevers.size() - 1);
      return false;
    }
  }

  public boolean containsDoorLever(Vector lever) {
    return doorLevers.contains(lever);
  }

  public boolean removeDoorLever(Vector lever) {
    if (!doorLevers.remove(lever)) {
      return false;
    } else if (save()) {
      reload();
      return true;
    } else {
      doorLevers.add(lever);
      return false;
    }
  }

  public Map<Integer, Floor> getFloors() {
    Map<Integer, Floor> clonedMap = new HashMap<>();
    Set<Map.Entry<Integer, Floor>> entries = floors.entrySet();
    for (Map.Entry<Integer, Floor> entry: entries) {
      clonedMap.put(entry.getKey(), entry.getValue());
    }
    return clonedMap;
  }

  public Floor getFloor(int floor) {
    return floors.get(floor);
  }

  private boolean addFloor(int floorLevel, Floor floor) {
    Floor oldFloor = floors.put(floorLevel, floor);
    if (save()) {
      if (oldFloor != null) {
        oldFloor.unload();
      }
      reload();
      return true;
    } else {
      floors.put(floorLevel, oldFloor);
      return false;
    }
  }

  public boolean containsFloor(int floorLevel) {
    return floors.containsKey(floorLevel);
  }

  public boolean removeFloor(int floorLevel) {
    Floor oldFloor = floors.remove(floorLevel);
    if (save()) {
      if (oldFloor != null) {
        oldFloor.unload();
        reload();
      }
      return oldFloor != null;
    } else {
      floors.put(floorLevel, oldFloor);
      return false;
    }
  }

  private void addFloorNoSave(int floorLevel, Floor floor) {
    floors.put(floorLevel, floor);
  }

  public static boolean create(JavaPlugin plugin, String name,
                               ElevatorManager manager,
                               Location loc1, Location loc2) {
    if (manager.containsElevator(name)) {
      throw new IllegalArgumentException("Elevator already exists: "
          + name);
    }
    return new Elevator(plugin, name, manager, loc1, loc2).save();
  }

  public boolean addStop(int floor) {
    if (!floors.containsKey(floor) || !loaded) {
      return false;
    }
    stops.add(floor);
    if (!active && masterBlock != null) {
      active = true;
      task = new Mover().runTaskTimer(plugin, 10, 10);
    }
    return true;
  }

  private void cancelTask() {
    if (active) {
      task.cancel();
      active = false;
    }
  }

  private void reload() {
    findBlocks();
    stops.clear();
    cancelTask();
  }

  void unload() {
    if (loaded) {
      cancelTask();
      HandlerList.unregisterAll(blockEventListener);
      loaded = false;
    }
  }

  @Override
  protected void finalize() {
    if (loaded) {
      unload();
      plugin.getLogger().warning("Elevator " + name + " was not unloaded!");
    }
  }

  private void findBlocks() {
    int l1x,l1y,l1z,l2x,l2y,l2z;
    l1x = loc1.getBlockX();
    l1y = loc1.getBlockY();
    l1z = loc1.getBlockZ();
    l2x = loc2.getBlockX();
    l2y = loc2.getBlockY();
    l2z = loc2.getBlockZ();
    elevatorBlocks.clear();
    masterBlock = null;
    for (int x=Math.min(l1x, l2x); x<=Math.max(l1x, l2x); x++) {
      for (int y=Math.min(l1y, l2y); y<=Math.max(l1y, l2y); y++) {
        for (int z=Math.min(l1z, l2z); z<=Math.max(l1z, l2z); z++) {
          Block block = Objects.requireNonNull(loc1.getWorld())
              .getBlockAt(x, y, z);
          if (!block.getType().equals(Material.AIR)) {
            elevatorBlocks.add(block.getLocation());
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

  private boolean save() {
    return manager.saveElevator(name, this);
  }

  private class BlockEventListener implements Listener {
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
      if (!event.isCancelled()) {
        handle(event);
      }
    }

    @EventHandler
    public void onBlockDamageEvent(BlockDamageEvent event) {
      if (!event.isCancelled()) {
        handle(event);
      }
    }

    public void handle(BlockEvent event) {
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

      if (x >= Math.min(l1x, l2x) && x <= Math.max(l1x, l2x) &&
          y >= Math.min(l1y, l2y) && y <= Math.max(l1y, l2y) &&
          z >= Math.min(l1z, l2z) && z <= Math.max(l1z, l2z)) {
        reload();
      }
    }
  }

  public static class Floor {
    private final JavaPlugin plugin;
    private final Elevator elevator;
    private final Location loc;
    private final int floorLevel;
    private final List<Location> doorLevers;
    private Location callButton;
    private CallButtonListener callButtonListener = null;
    private boolean loaded = true;

    private Floor(JavaPlugin plugin, Elevator elevator,
                  int floorLevel, Location loc) {
      this.plugin = plugin;
      this.elevator = elevator;
      this.loc = loc;
      this.floorLevel = floorLevel;
      this.doorLevers = new ArrayList<>();
    }

    Floor(JavaPlugin plugin, Elevator elevator,
          int floorLevel, Location loc,
          List<Location> doorLevers, Location callButton) {
      this.plugin = plugin;
      this.elevator = elevator;
      this.loc = loc;
      this.floorLevel = floorLevel;
      this.doorLevers = doorLevers;
      this.callButton = callButton;
      reload();
      elevator.addFloorNoSave(floorLevel, this);
    }

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

    private void unload() {
      if (loaded) {
        if (callButtonListener != null) {
          HandlerList.unregisterAll(callButtonListener);
          callButtonListener = null;
        }
        loaded = false;
      }
    }

    @Override
    protected void finalize() {
      if (loaded) {
        unload();
        plugin.getLogger().warning("Floor " + floorLevel + " of elevator "
            + elevator.getName() + " was not unloaded!");
      }
    }

    public static boolean create(JavaPlugin plugin, Elevator elevator,
                                 int floorLevel, Location loc) {
      return elevator.addFloor(floorLevel, new Floor(plugin, elevator,
          floorLevel, loc));
    }

    public Location getLocation() {
      return loc;
    }

    public List<Location> getDoorLevers() {
      List<Location> clonedList = new ArrayList<>();
      for (Location lever: doorLevers) {
        clonedList.add(lever.clone());
      }
      return clonedList;
    }

    public boolean addDoorLever(Location loc) {
      doorLevers.add(loc);
      if (save()) {
        elevator.reload();
        return true;
      } else {
        doorLevers.remove(doorLevers.size() - 1);
        return false;
      }
    }

    public boolean containsDoorLever(int x, int y, int z) {
      for (Location lever: doorLevers) {
        if (lever.getBlockX() == x && lever.getBlockY() == y
            && lever.getBlockZ() == z) {
          return true;
        }
      }
      return false;
    }

    public boolean removeDoorLever(int x, int y, int z) {
      for (Location loc: doorLevers) {
        if (loc.getBlockX() == x && loc.getBlockY() == y
            && loc.getBlockZ() == z) {
          doorLevers.remove(loc);
          if (save()) {
            elevator.reload();
            return true;
          } else {
            doorLevers.add(loc);
            return false;
          }
        }
      }
      return false;
    }

    public boolean setCallButton(Location callButton) {
      Location oldCallButton = this.callButton;
      this.callButton = callButton;
      if (save()) {
        reload();
        return true;
      } else {
        this.callButton = oldCallButton;
        return false;
      }
    }

    public Location getCallButton() {
      return callButton;
    }

    private class CallButtonListener implements Listener {
      @EventHandler
      public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
          Location blockLoc = Objects.requireNonNull(event.getClickedBlock())
              .getLocation();
          if (blockLoc.getBlockX() == callButton.getBlockX() &&
              blockLoc.getBlockY() == callButton.getBlockY() &&
              blockLoc.getBlockZ() == callButton.getBlockZ() &&
              blockLoc.getBlock().getBlockData() instanceof Switch) {
            if (elevator.addStop(floorLevel)) {
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

    public boolean save() {
      return elevator.save();
    }
  }

  private class Mover extends BukkitRunnable {
    // 1 - up, -1 - down
    private byte direction = 0;
    private int delay = 0;
    private static final int TICK_INTERVAL = 10;

    @Override
    public void run() {
      if (delay > 0) {
        delay--;
        return;
      }

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

      if (stops.isEmpty()) {
        cancelTask();
        return;
      }

      if (direction == 0) {
        direction = -1;
        stops.stream().map(floors::get)
            .map(floor -> floor.getLocation().getBlockY())
            .min(Comparator.comparingInt(y ->
                Math.abs(y - masterBlock.getBlockY())))
            .filter(y -> y > masterBlock.getBlockY())
            .ifPresent(y -> direction = 1);
      }

      if (stops.stream().map(floors::get)
          .anyMatch(floor -> floor.getLocation().getBlockX() ==
              masterBlock.getBlockX() &&
              floor.getLocation().getBlockY() == masterBlock.getBlockY() &&
              floor.getLocation().getBlockZ() == masterBlock.getBlockZ())) {
        delay = 5 * 20 / TICK_INTERVAL;
        setCurrentFloor();
        // open doors
        setDoorsState(true);
        stops.remove(currentFloor);
        return;
      }

      if (direction == 1) {
        if (masterBlock.getBlockY()+1 >
            Math.max(loc1.getBlockY(), loc2.getBlockY())) {
          plugin.getLogger().warning("Elevator " + name + " was going up " +
              "and reached max height, but still didn't arrive at a stop! " +
              "Please check if floor coordinates are correct.");
          cancelTask();
        } else {
          moveBlocks(1);
        }
      } else {
        if (masterBlock.getBlockY()-1 <
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

    private void moveBlocks(int num) {
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
      ArrayList<Integer> breakingBlocksIndex = new ArrayList<>();
      ArrayList<BlockData> breakingBlocks = new ArrayList<>();
      for (int i=0; i< elevatorBlocks.size(); i++) {
        Material material =
            elevatorBlocks.get(i).getBlock().getBlockData().getMaterial();
        if (material == Material.LEVER || material == Material.IRON_DOOR) {
          breakingBlocksIndex.add(i);
          breakingBlocks.add(elevatorBlocks.get(i).getBlock().getBlockData());
        }
      }
      for (Integer index : breakingBlocksIndex) {
        elevatorBlocks.get(index).getBlock().setType(Material.AIR);
        elevatorBlocks.get(index).add(0, num, 0);
      }
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
      for (int i=0; i<breakingBlocks.size(); i++) {
        elevatorBlocks.get(breakingBlocksIndex.get(i)).getBlock().setBlockData
         (breakingBlocks.get(i));
      }
      masterBlock.add(0, num, 0);
    }

    private void moveBlock(int i, int num) {
      new Location(elevatorBlocks.get(i).getWorld(),
          elevatorBlocks.get(i).getX(),
          elevatorBlocks.get(i).getY() + num,
          elevatorBlocks.get(i).getZ()).getBlock().setBlockData(
          elevatorBlocks.get(i).getBlock().getBlockData());
      elevatorBlocks.get(i).getBlock().setType(Material.AIR);
    }

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

    private void setDoorsState(boolean open) {
      if (open == doorsOpen) {
        return;
      }

      Floor floor = floors.get(currentFloor);
      if (floor.getDoorLevers().stream().map(doorLever ->
              doorLever.getBlock().getBlockData().getMaterial())
          .anyMatch(material -> material != Material.LEVER)) {
        plugin.getLogger().warning("One of floor " + currentFloor + " of " +
            "elevator" + name + "'s door levers material type not lever!");
      }
      if (doorLevers.stream().map(doorLever -> masterBlock.clone()
              .add(doorLever)).map(Location::getBlock)
          .anyMatch(block -> block.getBlockData().getMaterial() !=
              Material.LEVER)) {
        plugin.getLogger().warning("One of elevator " + name + "'s door " +
            "levers material type not lever!");
      }
      floor.getDoorLevers().stream()
          .map(Location::getBlock)
          .filter(block -> block.getBlockData().getMaterial() ==
              Material.LEVER)
          .forEach(block -> {
            Switch aSwitch = (Switch) block.getBlockData();
            aSwitch.setPowered(open);
            block.setBlockData(aSwitch);
          });
      doorLevers.stream().map(doorLever -> masterBlock.clone()
              .add(doorLever)).map(Location::getBlock)
          .filter(block -> block.getBlockData().getMaterial() ==
              Material.LEVER)
          .forEach(block -> {
            Switch aSwitch = (Switch) block.getBlockData();
            aSwitch.setPowered(open);
            block.setBlockData(aSwitch);
          });
      doorsOpen = open;
    }
  }
}
