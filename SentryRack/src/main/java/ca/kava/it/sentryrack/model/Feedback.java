// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

public class Feedback {
    private String id;
    private String userId;
    private String message;
    private int rating;
    private String category;
    private String locationId;
    private String buildingId;
    private long timestamp;
    private String status;

    public Feedback() {
    }

    public Feedback(String id, String userId, String message, int rating, String category, String locationId, String buildingId, long timestamp, String status) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.rating = rating;
        this.category = category;
        this.locationId = locationId;
        this.buildingId = buildingId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}