// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

public class Feedback {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String comment;
    private int starRating;
    private DeviceInfo deviceInfo;

    public Feedback() {
    }

    public Feedback(String fullName, String phoneNumber, String email, String comment,
                    int starRating, DeviceInfo deviceInfo) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.comment = comment;
        this.starRating = starRating;
        this.deviceInfo = deviceInfo;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getStarRating() { return starRating; }
    public void setStarRating(int starRating) { this.starRating = starRating; }

    public DeviceInfo getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(DeviceInfo deviceInfo) { this.deviceInfo = deviceInfo; }
}