package ca.kava.it.sentryrack;

import org.junit.Test;

import java.util.Collections;

import ca.kava.it.sentryrack.model.RoomData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RoomDataUnitTest {

    @Test
    public void constructorStoresRoomFieldsAndInitializesRacks() {
        RoomData room = new RoomData("room-1", "floor-1", "building-1",
                "location-1", "Server room");

        assertEquals("room-1", room.getId());
        assertEquals("floor-1", room.getFloorId());
        assertEquals("building-1", room.getBuildingId());
        assertEquals("location-1", room.getLocationId());
        assertEquals("Server room", room.getName());
        assertNotNull(room.getRacks());
    }

    @Test
    public void settersReplaceRoomFieldsAndRacks() {
        RoomData room = new RoomData();
        room.setId("room-2");
        room.setFloorId("floor-2");
        room.setBuildingId("building-2");
        room.setLocationId("location-2");
        room.setName("Network room");
        room.setRacks(Collections.singletonMap("rack-2", true));

        assertEquals("room-2", room.getId());
        assertEquals("floor-2", room.getFloorId());
        assertEquals("building-2", room.getBuildingId());
        assertEquals("location-2", room.getLocationId());
        assertEquals("Network room", room.getName());
        assertEquals(Collections.singletonMap("rack-2", true), room.getRacks());
    }

    @Test
    public void defaultInstancesDoNotShareRackMaps() {
        RoomData first = new RoomData();
        RoomData second = new RoomData();
        first.getRacks().put("rack-1", true);

        assertNull(second.getRacks().get("rack-1"));
    }

    @Test
    public void nullableFieldsAndRacksMapArePreserved() {
        RoomData room = new RoomData(null, null, null, null, null);
        room.setRacks(null);

        assertNull(room.getId());
        assertNull(room.getFloorId());
        assertNull(room.getBuildingId());
        assertNull(room.getLocationId());
        assertNull(room.getName());
        assertNull(room.getRacks());
    }
}
