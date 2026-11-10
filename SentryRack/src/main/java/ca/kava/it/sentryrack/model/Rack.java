// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

public class Rack {
    private String id;
    private String roomId;
    private String floorId;
    private String buildingId;
    private String locationId;
    private String name;
    private boolean online;
    private long lastSeen;

    public Rack() {
    }

    public Rack(String id, String roomId, String floorId, String buildingId, String locationId, String name, boolean online, long lastSeen) {
        this.id = id;
        this.roomId = roomId;
        this.floorId = floorId;
        this.buildingId = buildingId;
        this.locationId = locationId;
        this.name = name;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getFloorId() { return floorId; }
    public void setFloorId(String floorId) { this.floorId = floorId; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }
}
