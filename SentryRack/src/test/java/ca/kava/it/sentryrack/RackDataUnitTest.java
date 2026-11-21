package ca.kava.it.sentryrack;

import org.junit.Test;

import ca.kava.it.sentryrack.model.RackData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RackDataUnitTest {

    @Test
    public void constructorStoresRackFields() {
        RackData rack = new RackData("rack-1", "room-1", "floor-1", "building-1",
                "location-1", "Rack 1", true, 123L);

        assertEquals("rack-1", rack.getId());
        assertEquals("room-1", rack.getRoomId());
        assertEquals("floor-1", rack.getFloorId());
        assertEquals("building-1", rack.getBuildingId());
        assertEquals("location-1", rack.getLocationId());
        assertEquals("Rack 1", rack.getName());
        assertTrue(rack.isOnline());
        assertEquals(123L, rack.getLastSeen());
    }

    @Test
    public void settersReplaceRackFields() {
        RackData rack = new RackData();
        rack.setId("rack-2");
        rack.setRoomId("room-2");
        rack.setFloorId("floor-2");
        rack.setBuildingId("building-2");
        rack.setLocationId("location-2");
        rack.setName("Rack 2");
        rack.setOnline(false);
        rack.setLastSeen(456L);

        assertEquals("rack-2", rack.getId());
        assertEquals("room-2", rack.getRoomId());
        assertEquals("floor-2", rack.getFloorId());
        assertEquals("building-2", rack.getBuildingId());
        assertEquals("location-2", rack.getLocationId());
        assertEquals("Rack 2", rack.getName());
        assertFalse(rack.isOnline());
        assertEquals(456L, rack.getLastSeen());
    }

    @Test
    public void nullableFieldsArePreserved() {
        RackData rack = new RackData(null, null, null, null, null, null, false, 0L);

        assertNull(rack.getId());
        assertNull(rack.getRoomId());
        assertNull(rack.getFloorId());
        assertNull(rack.getBuildingId());
        assertNull(rack.getLocationId());
        assertNull(rack.getName());
    }
}
