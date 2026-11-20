package ca.kava.it.sentryrack;

import org.junit.Test;

import java.util.Collections;

import ca.kava.it.sentryrack.model.UserData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserDataUnitTest {

    @Test
    public void constructorStoresProfileAndTimestampFields() {
        UserData user = new UserData("user-1", "Alex", "alex@example.com", 10L, 20L);

        assertEquals("user-1", user.getUid());
        assertEquals("Alex", user.getName());
        assertEquals("Alex", user.getDisplayName());
        assertEquals("alex@example.com", user.getEmail());
        assertEquals(10L, user.getCreatedAt());
        assertEquals(20L, user.getLastLoginAt());
    }

    @Test
    public void noArgumentConstructorInitializesMembershipMaps() {
        UserData user = new UserData();

        assertNotNull(user.getLocations());
        assertNotNull(user.getBuildings());
        assertNotNull(user.getFeedback());
    }

    @Test
    public void settersReplaceProfileFieldsAndMaps() {
        UserData user = new UserData();
        user.setUid("user-2");
        user.setName("Sam");
        user.setEmail("sam@example.com");
        user.setCreatedAt(30L);
        user.setLastLoginAt(40L);
        user.setLocations(Collections.singletonMap("location-1", true));
        user.setBuildings(Collections.singletonMap("building-1", true));
        user.setFeedback(Collections.singletonMap("feedback-1", true));

        assertEquals("user-2", user.getUid());
        assertEquals("Sam", user.getName());
        assertEquals("Sam", user.getDisplayName());
        assertEquals("sam@example.com", user.getEmail());
        assertEquals(30L, user.getCreatedAt());
        assertEquals(40L, user.getLastLoginAt());
        assertEquals(Collections.singletonMap("location-1", true), user.getLocations());
        assertEquals(Collections.singletonMap("building-1", true), user.getBuildings());
        assertEquals(Collections.singletonMap("feedback-1", true), user.getFeedback());
    }

    @Test
    public void nullFirebaseUserProducesNullUserData() {
        assertNull(UserData.fromFirebaseUser(null));
    }

    @Test
    public void defaultMembershipMapsAreNotSharedBetweenUsers() {
        UserData first = new UserData();
        UserData second = new UserData();
        first.getLocations().put("location-1", true);
        first.getBuildings().put("building-1", true);
        first.getFeedback().put("feedback-1", true);

        assertNull(second.getLocations().get("location-1"));
        assertNull(second.getBuildings().get("building-1"));
        assertNull(second.getFeedback().get("feedback-1"));
    }

    @Test
    public void nullableFieldsAndMapsArePreserved() {
        UserData user = new UserData(null, null, null, 0L, 0L);
        user.setLocations(null);
        user.setBuildings(null);
        user.setFeedback(null);

        assertNull(user.getUid());
        assertNull(user.getName());
        assertNull(user.getDisplayName());
        assertNull(user.getEmail());
        assertNull(user.getLocations());
        assertNull(user.getBuildings());
        assertNull(user.getFeedback());
    }
}
