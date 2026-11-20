package ca.kava.it.sentryrack;

import org.junit.Test;

import java.util.Collections;

import ca.kava.it.sentryrack.model.BuildingData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BuildingDataUnitTest {

    @Test
    public void constructorStoresBuildingFieldsAndInitializesMaps() {
        BuildingData building = new BuildingData("building-1", "location-1", "Main");

        assertEquals("building-1", building.getId());
        assertEquals("location-1", building.getLocationId());
        assertEquals("Main", building.getName());
        assertNotNull(building.getMembers());
        assertNotNull(building.getFloors());
    }

    @Test
    public void settersReplaceFieldsAndMaps() {
        BuildingData building = new BuildingData();
        building.setId("building-2");
        building.setLocationId("location-2");
        building.setName("Annex");
        building.setMembers(Collections.singletonMap("user-1", "admin"));
        building.setFloors(Collections.singletonMap("floor-1", true));

        assertEquals("building-2", building.getId());
        assertEquals("location-2", building.getLocationId());
        assertEquals("Annex", building.getName());
        assertEquals(Collections.singletonMap("user-1", "admin"), building.getMembers());
        assertEquals(Collections.singletonMap("floor-1", true), building.getFloors());
    }

    @Test
    public void defaultInstancesDoNotShareMembershipMaps() {
        BuildingData first = new BuildingData();
        BuildingData second = new BuildingData();
        first.getMembers().put("user-1", "admin");
        first.getFloors().put("floor-1", true);

        assertNull(second.getMembers().get("user-1"));
        assertNull(second.getFloors().get("floor-1"));
    }

    @Test
    public void nullableFieldsAndMapsArePreserved() {
        BuildingData building = new BuildingData(null, null, null);
        building.setMembers(null);
        building.setFloors(null);

        assertNull(building.getId());
        assertNull(building.getLocationId());
        assertNull(building.getName());
        assertNull(building.getMembers());
        assertNull(building.getFloors());
    }
}
