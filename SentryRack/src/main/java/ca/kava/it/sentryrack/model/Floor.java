// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import java.util.HashMap;
import java.util.Map;

public class Floor {
    private String id;
    private String buildingId;
    private String locationId;
    private int level;
    private String name;
    private Map<String, Boolean> rooms = new HashMap<>();

    public Floor() {
    }

    public Floor(String id, String buildingId, String locationId, int level, String name) {
        this.id = id;
        this.buildingId = buildingId;
        this.locationId = locationId;
        this.level = level;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Boolean> getRooms() { return rooms; }
    public void setRooms(Map<String, Boolean> rooms) { this.rooms = rooms; }
}