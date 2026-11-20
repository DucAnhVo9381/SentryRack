// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import android.os.Build;

public class DeviceInfoData {
    private String appVersion;
    private String androidVersion;
    private String manufacturer;
    private String model;

    public DeviceInfoData() {
    }

    public DeviceInfoData(String appVersion, String androidVersion, String manufacturer, String model) {
        this.appVersion = appVersion;
        this.androidVersion = androidVersion;
        this.manufacturer = manufacturer;
        this.model = model;
    }

    public static DeviceInfoData from(String appVersion) {
        return new DeviceInfoData(appVersion, Build.VERSION.RELEASE, Build.MANUFACTURER, Build.MODEL);
    }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public String getAndroidVersion() { return androidVersion; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}