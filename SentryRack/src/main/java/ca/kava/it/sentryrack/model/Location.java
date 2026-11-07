// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import java.util.HashMap;
import java.util.Map;

public class Location {
    private String id;
    private String name;
    private String address;
    private Map<String, String> members = new HashMap<>(); // "uid": "admin"|"technician"
    private Map<String, Boolean> buildings = new HashMap<>();

    public Location() {
    }

    public Location(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Map<String, String> getMembers() { return members; }
    public void setMembers(Map<String, String> members) { this.members = members; }

    public Map<String, Boolean> getBuildings() { return buildings; }
    public void setBuildings(Map<String, Boolean> buildings) { this.buildings = buildings; }
}