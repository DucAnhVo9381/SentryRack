// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import java.util.HashMap;
import java.util.Map;

public class Building {
    private String id;
    private String locationId;
    private String name;
    private Map<String, String> members = new HashMap<>(); // "uid": "admin"|"technician"
    private Map<String, Boolean> floors = new HashMap<>();

    public Building() {
    }

    public Building(String id, String locationId, String name) {
        this.id = id;
        this.locationId = locationId;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, String> getMembers() { return members; }
    public void setMembers(Map<String, String> members) { this.members = members; }

    public Map<String, Boolean> getFloors() { return floors; }
    public void setFloors(Map<String, Boolean> floors) { this.floors = floors; }
}