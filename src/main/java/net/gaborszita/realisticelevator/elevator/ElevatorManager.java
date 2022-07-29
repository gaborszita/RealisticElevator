/**
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is responsible for managing elevators.
 */
public class ElevatorManager {
  /**
   * Plugin instance. Used for logging and creating the elevators.
   */
  private final JavaPlugin plugin;

  /**
   * The elevators.
   */
  private Map<String, Elevator> elevators;

  /**
   * Name of the manager's config file.
   */
  private static final String elevatorsFileName = "elevators.json";

  /**
   * Path of the manager's config file.
   */
  private final File elevatorsFile;

  /**
   * Elevators file version number.
   */
  private static final int ELEVATORS_FILE_VERSION = 1;

  /**
   * Constructor. Creates a new ElevatorManager instance.
   *
   * @param plugin The plugin instance.
   */
  public ElevatorManager(@Nonnull JavaPlugin plugin) {
    this.plugin = plugin;
    elevatorsFile =  new File(plugin.getDataFolder(), elevatorsFileName);
    createElevatorsFileIfNotExists();
    loadElevators();
  }

  /**
   * Checks if the manager contains an elevator with the given name.
   *
   * @param name Name of the elevator.
   * @return True if the manager contains an elevator with the given name,
   *         false otherwise.
   */
  public boolean containsElevator(@Nonnull String name) {
    return elevators.containsKey(name);
  }

  /**
   * Deletes an elevator from the manager.
   *
   * @param name Name of the elevator.
   * @return True on success, false on failure.
   */
  public boolean deleteElevator(@Nonnull String name) {
    Elevator elevator = elevators.remove(name);
    if (elevator != null && saveElevator(name, null)) {
      elevator.unload();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets an elevator from the manager.
   *
   * @param name Name of the elevator.
   * @return The elevator with the given name, or null if it does not exist.
   */
  @Nullable
  public Elevator getElevator(@Nonnull String name) {
    return elevators.get(name);
  }

  /**
   * Gets all elevators from the manager.
   *
   * @return All elevators.
   */
  @Nonnull
  public Map<String, Elevator> getElevators() {
    return new HashMap<>(elevators);
  }

  /**
   * Saves an elevator to the config file.
   *
   * @param name Name of the elevator.
   * @param elevator Elevator object.
   * @return True on success, false on failure.
   */
  boolean saveElevator(String name, Elevator elevator) {
    // load file
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
      // check version
      int version = main.getInt("version");
      if (version != ELEVATORS_FILE_VERSION) {
        plugin.getLogger().severe("Unable to load elevators file. " +
            "File version is incorrect. " +
            "Expected: " + ELEVATORS_FILE_VERSION + " " +
            "Found: " + version);
        return false;
      }
      // get the main elevators object
      JSONArray elevatorsJson = main.getJSONArray("elevators");
      JSONObject elevatorJson = null;
      // search for the elevator to update in the file
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
        // if elevator object is null, meaning we want to delete it, remove it
        // from the file
        if (elevatorJson != null) {
          elevatorsJson.remove(i);
        }
      } else {
        // if elevator object is not null, update/add it to the file
        if (elevatorJson == null) {
          elevatorJson = new JSONObject();
          elevatorJson.put("name", name);
          elevatorsJson.put(elevatorJson);
        }
        // first location of the elevator
        Location loc1 = elevator.getLoc1();
        // get the UID of the world the elevator is in to store it in the file
        String worldUID = Objects.requireNonNull(loc1.getWorld()).getUID()
            .toString();
        elevatorJson.put("world", worldUID);
        int[] loc1Arr = {loc1.getBlockX(), loc1.getBlockY(),
            loc1.getBlockZ()};
        JSONArray loc1Json = new JSONArray(loc1Arr);
        elevatorJson.put("loc1", loc1Json);
        // second location of the elevator
        Location loc2 = elevator.getLoc2();
        int[] loc2Arr = {loc2.getBlockX(), loc2.getBlockY(),
            loc2.getBlockZ()};
        JSONArray loc2Json = new JSONArray(loc2Arr);
        elevatorJson.put("loc2", loc2Json);
        // door levers of the elevator
        // iterate through the door levers of the elevator and add them to the
        // file
        JSONArray doorLeversJson = new JSONArray();
        for (Vector doorLever : elevator.getDoorLevers()) {
          int[] doorLeverArr = {doorLever.getBlockX(),
              doorLever.getBlockY(), doorLever.getBlockZ()};
          JSONArray doorLeverJson = new JSONArray(doorLeverArr);
          doorLeversJson.put(doorLeverJson);
        }
        elevatorJson.put("doorLevers", doorLeversJson);
        // floors of the elevator
        JSONArray floorsJson = new JSONArray();
        Set<Map.Entry<Integer, Elevator.Floor>> floors = elevator.getFloors()
            .entrySet();
        // iterate through the floors
        for (Map.Entry<Integer, Elevator.Floor> floor : floors) {
          JSONObject floorJson = new JSONObject();
          // floor number
          floorJson.put("floor", floor.getKey());
          Location loc = floor.getValue().getLocation();
          // floor location
          int[] locArr = {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
          JSONArray locJson = new JSONArray(locArr);
          floorJson.put("loc", locJson);
          // door levers of the floor
          JSONArray floorDoorLeversJson = new JSONArray();
          for (Location doorLever : floor.getValue().getDoorLevers()) {
            int[] doorLeverArr = {doorLever.getBlockX(),
                doorLever.getBlockY(), doorLever.getBlockZ()};
            JSONArray floorDoorLeverJson = new JSONArray(doorLeverArr);
            floorDoorLeversJson.put(floorDoorLeverJson);
          }
          // call button of the floor
          Location callButton = floor.getValue().getCallButton();
          // if the call button is null, meaning there isn't a call button on
          // this floor, add null to the file
          if (callButton == null) {
            floorJson.put("callButton", JSONObject.NULL);
          } else {
            int[] callButtonArr = { callButton.getBlockX(),
                callButton.getBlockY(), callButton.getBlockZ() };
            JSONArray callButtonJson = new JSONArray(callButtonArr);
            floorJson.put("callButton", callButtonJson);
          }
          floorJson.put("doorLevers", floorDoorLeversJson);
          // add the floor to the floors of the elevator
          floorsJson.put(floorJson);
        }
        elevatorJson.put("floors", floorsJson);
      }
      // write to the file
      try (FileWriter writer = new FileWriter(elevatorsFile)) {
        main.write(writer);
      } catch (IOException e) {
        plugin.getLogger().severe("Failed to write to elevators file."
            + System.lineSeparator() + e);
        return false;
      }
      // if elevator is not null, add it to the map
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

  /**
   * Loads all elevators from the config file.
   */
  private void loadElevators() {
    elevators = new HashMap<>();
    // load file
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
      // check version
      int version = main.getInt("version");
      if (version != ELEVATORS_FILE_VERSION) {
        plugin.getLogger().severe("Unable to load elevators file. " +
            "File version is incorrect. " +
            "Expected: " + ELEVATORS_FILE_VERSION + " " +
            "Found: " + version);
        return;
      }
      // get main elevators object
      JSONArray elevatorsJson = main.getJSONArray("elevators");
      for (int i=0; i<elevatorsJson.length(); i++) {
        JSONObject elevatorJson = elevatorsJson.getJSONObject(i);
        // elevator name
        String name = elevatorJson.getString("name");
        // get world object from UID stored in file
        World world = Bukkit.getWorld(UUID.fromString(elevatorJson.getString(
            "world")));
        // first location of the elevator
        JSONArray loc1Json = elevatorJson.getJSONArray("loc1");
        Location loc1 = new Location(world, loc1Json.getInt(0),
            loc1Json.getInt(1),
            loc1Json.getInt(2));
        // second location of the elevator
        JSONArray loc2Json = elevatorJson.getJSONArray("loc2");
        Location loc2 = new Location(world, loc2Json.getInt(0),
            loc2Json.getInt(1),
            loc2Json.getInt(2));
        // door levers of the elevator
        JSONArray doorLeversJson = elevatorJson.getJSONArray("doorLevers");
        List<Vector> doorLevers = new ArrayList<>();
        for (int x=0; x<doorLeversJson.length(); x++) {
          JSONArray doorLeverJson = doorLeversJson.getJSONArray(x);
          doorLevers.add(new Vector(doorLeverJson.getInt(0),
              doorLeverJson.getInt(1), doorLeverJson.getInt(2)));
        }
        Elevator elevator = new Elevator(plugin, name, this, loc1, loc2,
            doorLevers);
        // floors
        JSONArray floorsJson = elevatorJson.getJSONArray("floors");
        for (int x=0; x<floorsJson.length(); x++) {
          JSONObject floorJson = floorsJson.getJSONObject(x);
          // floor number
          int floorNumber = floorJson.getInt("floor");
          // floor location
          JSONArray locJson = floorJson.getJSONArray("loc");
          Location loc = new Location(world, locJson.getInt(0),
              locJson.getInt(1), locJson.getInt(2));
          // door levers of the floor
          JSONArray floorDoorLeversJson = floorJson.getJSONArray("doorLevers");
          List<Location> floorDoorLevers = new ArrayList<>();
          for (int j=0; j<floorDoorLeversJson.length(); j++) {
            JSONArray floorDoorLeverJson = floorDoorLeversJson.getJSONArray(j);
            floorDoorLevers.add(new Location(world,
                floorDoorLeverJson.getInt(0), floorDoorLeverJson.getInt(1),
                floorDoorLeverJson.getInt(2)));
          }
          // call button of the floor
          Location callButton = null;
          // only load the call button if it is not null
          if (!floorJson.isNull("callButton")) {
            JSONArray callButtonJson = floorJson.getJSONArray("callButton");
            callButton = new Location(world, callButtonJson.getInt(0),
                callButtonJson.getInt(1), callButtonJson.getInt(2));
          }
          new Elevator.Floor(plugin, elevator, floorNumber, loc,
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

  /**
   * Creates the elevators file if it does not exist.
   */
  private void createElevatorsFileIfNotExists() {
    if (!elevatorsFile.exists()) {
      createElevatorsFile();
    }
  }

  /**
   * Creates the elevators file.
   */
  private void createElevatorsFile() {
    if (elevatorsFile.getParentFile().exists() ||
        elevatorsFile.getParentFile().mkdirs()) {
      JSONObject main = new JSONObject();
      // version number of the config file - reserved for future use
      main.put("version", ELEVATORS_FILE_VERSION);
      // main elevators object
      JSONArray elevators = new JSONArray();
      main.put("elevators", elevators);
      // write to the file
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
