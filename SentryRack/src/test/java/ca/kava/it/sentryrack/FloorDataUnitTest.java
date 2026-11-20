package ca.kava.it.sentryrack;

import org.junit.Test;

import java.util.Collections;

import ca.kava.it.sentryrack.model.FloorData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FloorDataUnitTest {

    @Test
    public void constructorStoresFloorFieldsAndInitializesRooms() {
        FloorData floor = new FloorData("floor-1", "building-1", "location-1", 2, "Level 2");

        assertEquals("floor-1", floor.getId());
        assertEquals("building-1", floor.getBuildingId());
        assertEquals("location-1", floor.getLocationId());
        assertEquals(2, floor.getLevel());
        assertEquals("Level 2", floor.getName());
        assertNotNull(floor.getRooms());
    }

    @Test
    public void settersReplaceFloorFieldsAndRooms() {
        FloorData floor = new FloorData();
        floor.setId("floor-3");
        floor.setBuildingId("building-3");
        floor.setLocationId("location-3");
        floor.setLevel(3);
        floor.setName("Level 3");
        floor.setRooms(Collections.singletonMap("room-3", true));

        assertEquals("floor-3", floor.getId());
        assertEquals("building-3", floor.getBuildingId());
        assertEquals("location-3", floor.getLocationId());
        assertEquals(3, floor.getLevel());
        assertEquals("Level 3", floor.getName());
        assertEquals(Collections.singletonMap("room-3", true), floor.getRooms());
    }

    @Test
    public void defaultInstancesDoNotShareRoomMaps() {
        FloorData first = new FloorData();
        FloorData second = new FloorData();
        first.getRooms().put("room-1", true);

        assertNull(second.getRooms().get("room-1"));
    }

    @Test
    public void nullableFieldsAndRoomsMapArePreserved() {
        FloorData floor = new FloorData(null, null, null, 0, null);
        floor.setRooms(null);

        assertNull(floor.getId());
        assertNull(floor.getBuildingId());
        assertNull(floor.getLocationId());
        assertNull(floor.getName());
        assertNull(floor.getRooms());
    }
}
