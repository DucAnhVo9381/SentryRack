package ca.kava.it.sentryrack;

import org.junit.Test;

import java.util.Collections;

import ca.kava.it.sentryrack.model.LocationData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LocationDataUnitTest {

    @Test
    public void constructorStoresLocationFieldsAndInitializesMaps() {
        LocationData location = new LocationData("location-1", "North", "Toronto");

        assertEquals("location-1", location.getId());
        assertEquals("North", location.getName());
        assertEquals("Toronto", location.getAddress());
        assertNotNull(location.getMembers());
        assertNotNull(location.getBuildings());
    }

    @Test
    public void settersReplaceLocationFieldsAndMaps() {
        LocationData location = new LocationData();
        location.setId("location-2");
        location.setName("South");
        location.setAddress("Ottawa");
        location.setMembers(Collections.singletonMap("user-1", "technician"));
        location.setBuildings(Collections.singletonMap("building-2", true));

        assertEquals("location-2", location.getId());
        assertEquals("South", location.getName());
        assertEquals("Ottawa", location.getAddress());
        assertEquals(Collections.singletonMap("user-1", "technician"), location.getMembers());
        assertEquals(Collections.singletonMap("building-2", true), location.getBuildings());
    }

    @Test
    public void defaultInstancesDoNotShareMembershipMaps() {
        LocationData first = new LocationData();
        LocationData second = new LocationData();
        first.getMembers().put("user-1", "admin");
        first.getBuildings().put("building-1", true);

        assertNull(second.getMembers().get("user-1"));
        assertNull(second.getBuildings().get("building-1"));
    }

    @Test
    public void nullableFieldsAndMapsArePreserved() {
        LocationData location = new LocationData(null, null, null);
        location.setMembers(null);
        location.setBuildings(null);

        assertNull(location.getId());
        assertNull(location.getName());
        assertNull(location.getAddress());
        assertNull(location.getMembers());
        assertNull(location.getBuildings());
    }
}
