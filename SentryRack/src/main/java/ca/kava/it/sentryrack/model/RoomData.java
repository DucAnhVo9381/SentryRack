// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import java.util.HashMap;
import java.util.Map;

public class RoomData {
    private String id;
    private String floorId;
    private String buildingId;
    private String locationId;
    private String name;
    private Map<String, Boolean> racks = new HashMap<>();

    public RoomData() {
    }

    public RoomData(String id, String floorId, String buildingId, String locationId, String name) {
        this.id = id;
        this.floorId = floorId;
        this.buildingId = buildingId;
        this.locationId = locationId;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFloorId() { return floorId; }
    public void setFloorId(String floorId) { this.floorId = floorId; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Boolean> getRacks() { return racks; }
    public void setRacks(Map<String, Boolean> racks) { this.racks = racks; }
}
