package net.gaborszita.realisticelevator.elevator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ElevatorManager {
  private final JavaPlugin plugin;
  private Map<String, Elevator> elevators;
  private static final String elevatorsFileName = "elevators.json";
  private final File elevatorsFile;

  public ElevatorManager(JavaPlugin plugin) {
    this.plugin = plugin;
    elevatorsFile =  new File(plugin.getDataFolder(), elevatorsFileName);
    createElevatorsFileIfNotExists();
    loadElevators();
  }

  public boolean containsElevator(String name) {
    return elevators.containsKey(name);
  }

  public boolean deleteElevator(String name) {
    Elevator elevator = elevators.remove(name);
    if (elevator != null && saveElevator(name, null)) {
      elevator.unload();
      return true;
    } else {
      return false;
    }
  }

  public Elevator getElevator(String name) {
    return elevators.get(name);
  }

  public Map<String, Elevator> getElevators() {
    return new HashMap<>(elevators);
  }

  boolean saveElevator(String name, Elevator elevator) {
    String content;
    try {
      content =
          new String(Files.readAllBytes(elevatorsFile.toPath()));
    } catch (IOException e) {
      plugin.getLogger().severe("Failed to load elevators file."
          + System.lineSeparator() + e);
      return false;
    }
    try {
      JSONObject main = new JSONObject(content);
      JSONArray elevatorsJson = main.getJSONArray("elevators");
      JSONObject elevatorJson = null;
      int i;
      for (i=0; i<elevatorsJson.length(); i++) {
        JSONObject currentElevatorJson = elevatorsJson.getJSONObject(i);
        String currentElevatorName = currentElevatorJson.getString("name");
        if (name.equals(currentElevatorName)) {
          elevatorJson = currentElevatorJson;
          break;
        }
      }
      if (elevator == null) {
        if (elevatorJson != null) {
          elevatorsJson.remove(i);
        }
      } else {
        if (elevatorJson == null) {
          elevatorJson = new JSONObject();
          elevatorJson.put("name", name);
          elevatorsJson.put(elevatorJson);
        }
        Location loc1 = elevator.getLoc1();
        String worldUID = Objects.requireNonNull(loc1.getWorld()).getUID()
            .toString();
        elevatorJson.put("world", worldUID);
        int[] loc1Arr = {loc1.getBlockX(), loc1.getBlockY(),
            loc1.getBlockZ()};
        JSONArray loc1Json = new JSONArray(loc1Arr);
        elevatorJson.put("loc1", loc1Json);
        Location loc2 = elevator.getLoc2();
        int[] loc2Arr = {loc2.getBlockX(), loc2.getBlockY(),
            loc2.getBlockZ()};
        JSONArray loc2Json = new JSONArray(loc2Arr);
        elevatorJson.put("loc2", loc2Json);
        JSONArray doorLeversJson = new JSONArray();
        for (Vector doorLever : elevator.getDoorLevers()) {
          int[] doorLeverArr = {doorLever.getBlockX(),
              doorLever.getBlockY(), doorLever.getBlockZ()};
          JSONArray doorLeverJson = new JSONArray(doorLeverArr);
          doorLeversJson.put(doorLeverJson);
        }
        elevatorJson.put("doorLevers", doorLeversJson);
        JSONArray floorsJson = new JSONArray();
        Set<Map.Entry<Integer, Elevator.Floor>> floors = elevator.getFloors()
            .entrySet();
        for (Map.Entry<Integer, Elevator.Floor> floor : floors) {
          JSONObject floorJson = new JSONObject();
          floorJson.put("floor", floor.getKey());
          Location loc = floor.getValue().getLocation();
          int[] locArr = {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
          JSONArray locJson = new JSONArray(locArr);
          floorJson.put("loc", locJson);
          JSONArray floorDoorLeversJson = new JSONArray();
          for (Location doorLever : floor.getValue().getDoorLevers()) {
            int[] doorLeverArr = {doorLever.getBlockX(),
                doorLever.getBlockY(), doorLever.getBlockZ()};
            JSONArray floorDoorLeverJson = new JSONArray(doorLeverArr);
            floorDoorLeversJson.put(floorDoorLeverJson);
          }
          Location callButton = floor.getValue().getCallButton();
          if (callButton == null) {
            floorJson.put("callButton", JSONObject.NULL);
          } else {
            int[] callbuttonArr = { callButton.getBlockX(),
                callButton.getBlockY(), callButton.getBlockZ() };
            JSONArray callButtonJson = new JSONArray(callbuttonArr);
            floorJson.put("callButton", callButtonJson);
          }
          floorJson.put("doorLevers", floorDoorLeversJson);
          floorsJson.put(floorJson);
        }
        elevatorJson.put("floors", floorsJson);
      }
      try (FileWriter writer = new FileWriter(elevatorsFile)) {
        main.write(writer);
      } catch (IOException e) {
        plugin.getLogger().severe("Failed to write to elevators file."
            + System.lineSeparator() + e);
        return false;
      }
      if (elevator != null) {
        elevators.put(name, elevator);
      }
    } catch (JSONException e) {
      plugin.getLogger().severe("Failed to parse elevators file."
          + System.lineSeparator() + e);
      return false;
    }
    return true;
  }

  private void loadElevators() {
    elevators = new HashMap<>();
    String content;
    try {
      content =
          new String(Files.readAllBytes(
              Paths.get(plugin.getDataFolder().getPath(), elevatorsFileName)));
    } catch (IOException e) {
      plugin.getLogger().severe("Failed to load elevators file."
          + System.lineSeparator() + e);
      return;
    }
    try {
      JSONObject main = new JSONObject(content);
      JSONArray elevatorsJson = main.getJSONArray("elevators");
      for (int i=0; i<elevatorsJson.length(); i++) {
        JSONObject elevatorJson = elevatorsJson.getJSONObject(i);
        String name = elevatorJson.getString("name");
        World world = Bukkit.getWorld(UUID.fromString(elevatorJson.getString(
            "world")));
        JSONArray loc1Json = elevatorJson.getJSONArray("loc1");
        Location loc1 = new Location(world, loc1Json.getInt(0),
            loc1Json.getInt(1),
            loc1Json.getInt(2));
        JSONArray loc2Json = elevatorJson.getJSONArray("loc2");
        Location loc2 = new Location(world, loc2Json.getInt(0),
            loc2Json.getInt(1),
            loc2Json.getInt(2));
        JSONArray doorLeversJson = elevatorJson.getJSONArray("doorLevers");
        List<Vector> doorLevers = new ArrayList<>();
        for (int x=0; x<doorLeversJson.length(); x++) {
          JSONArray doorLeverJson = doorLeversJson.getJSONArray(x);
          doorLevers.add(new Vector(doorLeverJson.getInt(0),
              doorLeverJson.getInt(1), doorLeverJson.getInt(2)));
        }
        Elevator elevator = new Elevator(plugin, name, this, loc1, loc2,
            doorLevers);
        JSONArray floorsJson = elevatorJson.getJSONArray("floors");
        for (int x=0; x<floorsJson.length(); x++) {
          JSONObject floorJson = floorsJson.getJSONObject(x);
          int floorLevel = floorJson.getInt("floor");
          JSONArray locJson = floorJson.getJSONArray("loc");
          Location loc = new Location(world, locJson.getInt(0),
              locJson.getInt(1), locJson.getInt(2));
          JSONArray floorDoorLeversJson = floorJson.getJSONArray("doorLevers");
          List<Location> floorDoorLevers = new ArrayList<>();
          for (int j=0; j<floorDoorLeversJson.length(); j++) {
            JSONArray floorDoorLeverJson = floorDoorLeversJson.getJSONArray(j);
            floorDoorLevers.add(new Location(world,
                floorDoorLeverJson.getInt(0), floorDoorLeverJson.getInt(1),
                floorDoorLeverJson.getInt(2)));
          }
          Location callButton = null;
          if (!floorJson.isNull("callButton")) {
            JSONArray callButtonJson = floorJson.getJSONArray("callButton");
            callButton = new Location(world, callButtonJson.getInt(0),
                callButtonJson.getInt(1), callButtonJson.getInt(2));
          }
          new Elevator.Floor(plugin, elevator, floorLevel, loc,
              floorDoorLevers, callButton);
        }
        elevators.put(name, elevator);
      }
    } catch (JSONException e) {
      plugin.getLogger().severe("Failed to parse elevators file."
          + System.lineSeparator() + e);
      elevators = new HashMap<>();
    }
  }

  private void createElevatorsFileIfNotExists() {
    if (!elevatorsFile.exists()) {
      createElevatorsFile();
    }
  }

  private void createElevatorsFile() {
    if (elevatorsFile.getParentFile().exists() ||
        elevatorsFile.getParentFile().mkdirs()) {
      JSONObject main = new JSONObject();
      main.put("version", 1);
      JSONArray elevators = new JSONArray();
      main.put("elevators", elevators);
      try (FileWriter writer = new FileWriter(elevatorsFile)) {
        main.write(writer);
      } catch (IOException e) {
        plugin.getLogger().severe("Failed to create elevators file."
            + System.lineSeparator() + e);
      }
    } else {
      plugin.getLogger().severe("Failed to create plugin data folder.");
    }
  }
}
