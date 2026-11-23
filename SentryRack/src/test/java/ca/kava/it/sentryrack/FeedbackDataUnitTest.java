package ca.kava.it.sentryrack;

import org.junit.Test;

import ca.kava.it.sentryrack.model.DeviceInfoData;
import ca.kava.it.sentryrack.model.FeedbackData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class FeedbackDataUnitTest {

    @Test
    public void constructorStoresFeedbackFieldsAndDeviceInfo() {
        DeviceInfoData device = new DeviceInfoData("1.0", "15", "Maker", "Model");
        FeedbackData feedback = new FeedbackData(
                "Alex", "1234567890", "alex@example.com", "Comment", 5.0, device);

        assertEquals("Alex", feedback.getFullName());
        assertEquals("1234567890", feedback.getPhoneNumber());
        assertEquals("alex@example.com", feedback.getEmail());
        assertEquals("Comment", feedback.getComment());
        assertEquals(5.0, feedback.getStarRating());
        assertSame(device, feedback.getDeviceInfo());
    }

    @Test
    public void settersReplaceFeedbackFields() {
        FeedbackData feedback = new FeedbackData();
        DeviceInfoData device = new DeviceInfoData("2.0", "16", "Maker", "New Model");

        feedback.setFullName("Sam");
        feedback.setPhoneNumber("5551234567");
        feedback.setEmail("sam@example.com");
        feedback.setComment("Updated");
        feedback.setStarRating(4.0);
        feedback.setDeviceInfo(device);

        assertEquals("Sam", feedback.getFullName());
        assertEquals("5551234567", feedback.getPhoneNumber());
        assertEquals("sam@example.com", feedback.getEmail());
        assertEquals("Updated", feedback.getComment());
        assertEquals(4.0, feedback.getStarRating());
        assertSame(device, feedback.getDeviceInfo());
    }

    @Test
    public void nullFieldsAndDeviceInfoArePreserved() {
        FeedbackData feedback = new FeedbackData(null, null, null, null, 0, null);

        assertNull(feedback.getFullName());
        assertNull(feedback.getPhoneNumber());
        assertNull(feedback.getEmail());
        assertNull(feedback.getComment());
        assertNull(feedback.getDeviceInfo());
        assertEquals(0.0, feedback.getStarRating());
    }
}
