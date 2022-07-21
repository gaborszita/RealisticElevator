package net.gaborszita.realisticelevator.elevator;

import net.gaborszita.realisticelevator.elevator.exceptions.ElevatorAlreadyExistsException;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
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

  private Elevator(JavaPlugin plugin, String name, ElevatorManager manager,
                   Location loc1, Location loc2) {
    if (manager.containsElevator(name)) {
      throw new ElevatorAlreadyExistsException("Elevator already exists: "
          + name);
    }
    this.plugin = plugin;
    this.name = name;
    this.manager = manager;
    this.loc1 = loc1;
    this.loc2 = loc2;
    this.doorLevers = new ArrayList<>();
    this.floors = new HashMap<>();
    save();
  }

  Elevator(JavaPlugin plugin, String name, ElevatorManager manager,
           Location loc1, Location loc2, List<Vector> doorLevers) {
    this.plugin = plugin;
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

  public Map<Integer, Floor> getFloors() {
    Map<Integer, Floor> clonedMap = new HashMap<>();
    Set<Map.Entry<Integer, Floor>> entries = floors.entrySet();
    for (Map.Entry<Integer, Floor> entry: entries) {
      clonedMap.put(entry.getKey(), entry.getValue().clone());
    }
    return clonedMap;
  }

  public boolean addFloor(int floorLevel, Floor floor) {
    Floor oldFloor = floors.put(floorLevel, floor);
    if (!save()) {
      floors.put(floorLevel, oldFloor);
      return false;
    } else {
      return true;
    }
  }

  public static boolean create(JavaPlugin plugin, String name,
                               ElevatorManager manager, Location loc1,
                               Location loc2) {
    return new Elevator(plugin, name, manager, loc1, loc2).save();
  }

  private boolean save() {
    return manager.saveElevator(name, this);
  }

  public class Floor implements Cloneable {
    Location loc;
    List<Location> doorLevers;

    public Floor(Location loc) {
      this.loc = loc;
      this.doorLevers = new ArrayList<>();
    }

    public Floor(Location loc, List<Location> doorLevers) {
      this.loc = loc;
      this.doorLevers = doorLevers;
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
