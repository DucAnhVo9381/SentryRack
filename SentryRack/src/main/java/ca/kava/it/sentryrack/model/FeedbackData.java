// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

public class FeedbackData {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String comment;
    private double starRating;
    private DeviceInfoData deviceInfoData;

    public FeedbackData() {
    }

    public FeedbackData(String fullName, String phoneNumber, String email, String comment,
                        int starRating, DeviceInfoData deviceInfoData) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.comment = comment;
        this.starRating = starRating;
        this.deviceInfoData = deviceInfoData;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public double getStarRating() { return starRating; }
    public void setStarRating(double starRating) { this.starRating = starRating; }

    public DeviceInfoData getDeviceInfo() { return deviceInfoData; }
    public void setDeviceInfo(DeviceInfoData deviceInfoData) { this.deviceInfoData = deviceInfoData; }
}