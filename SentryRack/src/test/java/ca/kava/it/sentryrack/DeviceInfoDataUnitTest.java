package ca.kava.it.sentryrack;

import org.junit.Test;

import ca.kava.it.sentryrack.model.DeviceInfoData;

import static org.junit.Assert.assertEquals;

public class DeviceInfoDataUnitTest {

    @Test
    public void constructorStoresDeviceFields() {
        DeviceInfoData device = new DeviceInfoData("1.0", "15", "Maker", "Model");

        assertEquals("1.0", device.getAppVersion());
        assertEquals("15", device.getAndroidVersion());
        assertEquals("Maker", device.getManufacturer());
        assertEquals("Model", device.getModel());
    }

    @Test
    public void settersReplaceDeviceFields() {
        DeviceInfoData device = new DeviceInfoData();
        device.setAppVersion("2.0");
        device.setAndroidVersion("16");
        device.setManufacturer("New Maker");
        device.setModel("New Model");

        assertEquals("2.0", device.getAppVersion());
        assertEquals("16", device.getAndroidVersion());
        assertEquals("New Maker", device.getManufacturer());
        assertEquals("New Model", device.getModel());
    }
}
