package net.gaborszita.realisticelevator.elevator;

import net.gaborszita.realisticelevator.elevator.exceptions.ElevatorAlreadyExistsException;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class Elevator {
  private final String name;
  private final ElevatorManager manager;
  private Location loc1;
  private Location loc2;
  private final List<Vector> doorLevers;
  private final Map<Integer, Floor> floors;

  private Elevator(String name, ElevatorManager manager,
                   Location loc1, Location loc2) {
    if (manager.containsElevator(name)) {
      throw new ElevatorAlreadyExistsException("Elevator already exists: "
          + name);
    }
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doorLevers = new ArrayList<>();
    this.floors = new HashMap<>();
  }

  Elevator(String name, ElevatorManager manager, Location loc1, Location loc2,
           List<Vector> doorLevers) {
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doorLevers = doorLevers;
    this.floors = new HashMap<>();
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

  public static boolean create(String name, ElevatorManager manager,
                               Location loc1, Location loc2) {
    return new Elevator(name, manager, loc1, loc2).save();
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
}
