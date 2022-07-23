package net.gaborszita.realisticelevator.elevator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
  BukkitTask task;
  private boolean active = false;
  private Location masterBlock;
  private ArrayList<Location> elevatorBlocks = new ArrayList<>();
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
  }

  public String getName() {
    return name;
  }

  public boolean setLocation(Location loc1, Location loc2) {
    Location oldLoc1 = this.loc1;
    Location oldLoc2 = this.loc2;
    this.loc1 = loc1;
    this.loc2 = loc2;
    if (!save()) {
      this.loc1 = oldLoc1;
      this.loc2 = oldLoc2;
      return false;
    } else {
      return true;
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
    if (!save()) {
      doorLevers.remove(doorLevers.size() - 1);
      return false;
    } else {
      return true;
    }
  }

  public boolean containsDoorLever(Vector lever) {
    return doorLevers.contains(lever);
  }

  public boolean removeDoorLever(Vector lever) {
    if (!doorLevers.remove(lever)) {
      return false;
    } else if (!save()) {
      doorLevers.add(lever);
      return false;
    } else {
      return true;
    }
  }

  public Map<Integer, Floor> getFloors() {
    Map<Integer, Floor> clonedMap = new HashMap<>();
    Set<Map.Entry<Integer, Floor>> entries = floors.entrySet();
    for (Map.Entry<Integer, Floor> entry: entries) {
      clonedMap.put(entry.getKey(), entry.getValue().clone());
    }
    return clonedMap;
  }

  public Floor getFloor(int floor) {
    return floors.get(floor);
  }

  private boolean addFloor(int floorLevel, Floor floor) {
    Floor oldFloor = floors.put(floorLevel, floor);
    if (!save()) {
      floors.put(floorLevel, oldFloor);
      return false;
    } else {
      return true;
    }
  }

  public boolean containsFloor(int floorLevel) {
    return floors.containsKey(floorLevel);
  }

  public boolean removeFloor(int floorLevel) {
    Floor oldFloor = floors.remove(floorLevel);
    if (!save()) {
      floors.put(floorLevel, oldFloor);
      return false;
    } else {
      return oldFloor != null;
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
    if (!floors.containsKey(floor)) {
      return false;
    }
    stops.add(floor);
    if (!active) {
      active = true;
      task = new Mover().runTaskTimer(plugin, 10, 10);
    }
    return true;
  }

  private void reload() {
    findBlocks();
    stops.clear();
    if (!active) {
      task.cancel();
      active = false;
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

  public static class Floor implements Cloneable {
    private final JavaPlugin plugin;
    private final Elevator elevator;
    private final Location loc;
    private List<Location> doorLevers;

    private Floor(JavaPlugin plugin, Elevator elevator, Location loc) {
      this.plugin = plugin;
      this.elevator = elevator;
      this.loc = loc;
      this.doorLevers = new ArrayList<>();
    }

    Floor(JavaPlugin plugin, Elevator elevator,
                 int floorLevel, Location loc,
                 List<Location> doorLevers) {
      this.plugin = plugin;
      this.elevator = elevator;
      this.loc = loc;
      this.doorLevers = doorLevers;
      elevator.addFloorNoSave(floorLevel, this);
    }

    public static boolean create(JavaPlugin plugin, Elevator elevator,
                                 int floorLevel, Location loc) {
      return elevator.addFloor(floorLevel, new Floor(plugin, elevator, loc));
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
      if (!save()) {
        doorLevers.remove(doorLevers.size() - 1);
        return false;
      } else {
        return true;
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
          if (!save()) {
            doorLevers.add(loc);
            return false;
          } else {
            return true;
          }
        }
      }
      return false;
    }

    public boolean save() {
      return elevator.save();
    }

    @Override
    public Floor clone() {
      Floor clonedFloor;
      try {
        clonedFloor = (Floor)super.clone();
      } catch (CloneNotSupportedException e) {
        plugin.getLogger().severe("Failed to clone Floor"
            + System.lineSeparator() + e);
        throw new RuntimeException(e);
      }
      clonedFloor.doorLevers = new ArrayList<>();
      for (Location loc: doorLevers) {
        clonedFloor.doorLevers.add(loc.clone());
      }
      return clonedFloor;
    }
  }

  private class Mover extends BukkitRunnable {
    // 1 - up, -1 - down
    private byte direction = 0;
    private int currentFloor = 0;

    @Override
    public void run() {
      if (stops.isEmpty()) {
        task.cancel();
        active = false;
        return;
      }

      moveToClosestFloor();

      for (int stop: stops) {
        stops.remove(stop);
        moveToFloor(stop);
      }


      task.cancel();
      active = false;
    }

    private void moveToClosestFloor() {
      floors.entrySet().stream().min(Comparator.comparingInt(entry ->
              Math.abs(entry.getValue().getLocation().getBlockY() -
                  masterBlock.getBlockY())))
          .ifPresent(entry -> moveToFloor(entry.getKey()));
    }

    private void moveToFloor(int floor) {
      moveBlocks(floors.get(floor).getLocation().getBlockY() -
          masterBlock.getBlockY());
      currentFloor = floors.get(floor).getLocation().getBlockY();
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
      if (num > 0) {
        for (int i=0; i< elevatorBlocks.size(); i++) {
          moveBlock(i, num);
          elevatorBlocks.set(i, elevatorBlocks.get(i).add(0, num, 0));
        }
      } else if (num < 0) {
        for (int i= elevatorBlocks.size()-1; i>=0; i--) {
          moveBlock(i, num);
          elevatorBlocks.set(i, elevatorBlocks.get(i).add(0, num, 0));
        }
      }
      masterBlock = masterBlock.add(0, num, 0);
    }

    private void moveBlock(int i, int num) {
      new Location(elevatorBlocks.get(i).getWorld(),
          elevatorBlocks.get(i).getX(),
          elevatorBlocks.get(i).getY() + num,
          elevatorBlocks.get(i).getZ()).getBlock().setBlockData(
          elevatorBlocks.get(i).getBlock().getBlockData());
      elevatorBlocks.get(i).getBlock().setType(Material.AIR);
    }
  }
}
