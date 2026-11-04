// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.auth.FirebaseUser;

/**
 * Domain model representing an authenticated user in SentryRack.
 * Note these fields are default in Firebase Auth
 */
public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private boolean isEmailVerified;

    public User() {
    }

    public User(String uid, String email, String displayName, String photoUrl, boolean isEmailVerified) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.isEmailVerified = isEmailVerified;
    }

    /**
     * Factory method mapping FirebaseUser to the domain User model
     * This maps the object to Firebase
     */
    public static User fromFirebaseUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            return null;
        }
        String photo = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;
        String name = firebaseUser.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            if (firebaseUser.getEmail() != null && firebaseUser.getEmail().contains("@")) {
                name = firebaseUser.getEmail().split("@")[0];
            } else {
                name = "User";
            }
        }
        return new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                name,
                photo,
                firebaseUser.isEmailVerified()
        );
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }
}