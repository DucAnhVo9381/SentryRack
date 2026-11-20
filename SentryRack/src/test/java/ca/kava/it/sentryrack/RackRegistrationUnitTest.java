package ca.kava.it.sentryrack;

import org.junit.Test;

import ca.kava.it.sentryrack.model.RackRegistration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RackRegistrationUnitTest {

    @Test
    public void constructorPreservesRegistrationRequestFields() {
        RackRegistration request = new RackRegistration("user-1", "admin", "HQ",
                "Building A", "Floor 2", 2, "Room 1", "Rack 1");

        assertEquals("user-1", request.uid);
        assertEquals("admin", request.role);
        assertEquals("HQ", request.locationName);
        assertEquals("Building A", request.buildingName);
        assertEquals("Floor 2", request.floorName);
        assertEquals(Integer.valueOf(2), request.floorLevel);
        assertEquals("Room 1", request.roomName);
        assertEquals("Rack 1", request.rackName);
    }

    @Test
    public void constructorPreservesMissingFloorLevel() {
        RackRegistration request = new RackRegistration("user-1", "admin", "HQ",
                "Building A", "Basement", null, "Room 1", "Rack 1");

        assertNull(request.floorLevel);
    }

    @Test
    public void constructorPreservesNullRequestFields() {
        RackRegistration request = new RackRegistration(
                null, null, null, null, null, null, null, null);

        assertNull(request.uid);
        assertNull(request.role);
        assertNull(request.locationName);
        assertNull(request.buildingName);
        assertNull(request.floorName);
        assertNull(request.floorLevel);
        assertNull(request.roomName);
        assertNull(request.rackName);
    }
}
