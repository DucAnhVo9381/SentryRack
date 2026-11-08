// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC

package ca.kava.it.sentryrack.model;


public class RackRegistration {
    public final String uid;
    public final String role;
    public final String locationName;
    public final String buildingName;
    public final String floorName;
    public final Integer floorLevel;
    public final String roomName;
    public final String rackName;

    public RackRegistration(String uid, String role, String locationName, String buildingName,
                            String floorName, Integer floorLevel, String roomName, String rackName) {
        this.uid = uid;
        this.role = role;
        this.locationName = locationName;
        this.buildingName = buildingName;
        this.floorName = floorName;
        this.floorLevel = floorLevel;
        this.roomName = roomName;
        this.rackName = rackName;
    }
}