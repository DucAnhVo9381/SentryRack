// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Model for the Account screen: reads and changes the signed-in user's profile.
 * - Firebase Authentication: display name, password, Google photo, delete sign-in
 * - Realtime Database users/{uid}: name, role, photo (small JPEG as Base64 text)
 * @author Anvesh Rachakonda
 * @date 2026-11-21
 */
public class ProfileRepository {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_PHOTO = "photo";
    /** Error text sent when Firebase wants a fresh sign-in before deleting. */
    public static final String ERROR_RELOGIN = "RELOGIN";

    private static final String PROVIDER_GOOGLE = "google.com";
    private static final String PROVIDER_PASSWORD = "password";

    public interface ResultCallback {
        void onResult(boolean success, @Nullable String error);
    }

    public interface ProfileCallback {
        void onLoaded(@Nullable String name, @Nullable String role, @Nullable String photo);
    }

    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public ProfileRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Nullable
    public FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    /** True when the user signed in with Google. */
    public boolean isGoogleAccount() {
        return hasProvider(PROVIDER_GOOGLE);
    }

    /** True when the user has an email + password sign-in (can change password). */
    public boolean hasPassword() {
        return hasProvider(PROVIDER_PASSWORD);
    }

    /** Google profile photo link from Firebase Authentication (null if none). */
    @Nullable
    public Uri getGooglePhotoUrl() {
        FirebaseUser user = getUser();
        if (user == null) return null;
        for (UserInfo info : user.getProviderData()) {
            if (PROVIDER_GOOGLE.equals(info.getProviderId()) && info.getPhotoUrl() != null) {
                return info.getPhotoUrl();
            }
        }
        return user.getPhotoUrl();
    }

    /** Reads users/{uid} once: name, role and photo. */
    public void loadProfile(@NonNull ProfileCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            callback.onLoaded(null, null, null);
            return;
        }
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onLoaded(snapshot.child(FIELD_NAME).getValue(String.class),
                        snapshot.child(FIELD_ROLE).getValue(String.class),
                        snapshot.child(FIELD_PHOTO).getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onLoaded(null, null, null);
            }
        });
    }

    /** Changes the name in Firebase Authentication AND in users/{uid}/name. */
    public void updateName(String name, @NonNull ResultCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            callback.onResult(false, "Not signed in");
            return;
        }
        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build())
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onResult(false, message(task.getException()));
                        return;
                    }
                    setField(FIELD_NAME, name, callback);
                });
    }

    /** Saves one field in users/{uid}; a null value removes it (e.g. remove photo). */
    public void setField(String field, @Nullable Object value, @NonNull ResultCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            callback.onResult(false, "Not signed in");
            return;
        }
        usersRef.child(user.getUid()).child(field).setValue(value)
                .addOnCompleteListener(task ->
                        callback.onResult(task.isSuccessful(), message(task.getException())));
    }

    /** Checks the current password with Firebase, then sets the new one. */
    public void changePassword(String currentPassword, String newPassword,
                               @NonNull ResultCallback callback) {
        FirebaseUser user = getUser();
        if (user == null || user.getEmail() == null) {
            callback.onResult(false, "Not signed in");
            return;
        }
        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword))
                .addOnCompleteListener(login -> {
                    if (!login.isSuccessful()) {
                        callback.onResult(false, message(login.getException()));
                        return;
                    }
                    user.updatePassword(newPassword).addOnCompleteListener(update ->
                            callback.onResult(update.isSuccessful(), message(update.getException())));
                });
    }

    /**
     * Deletes the sign-in account and then users/{uid}.
     * Email accounts confirm with their password first (Firebase needs a recent sign-in).
     * Racks, locations and feedback are kept for the team.
     */
    public void deleteAccount(@Nullable String password, @NonNull ResultCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            callback.onResult(false, "Not signed in");
            return;
        }
        if (hasPassword() && user.getEmail() != null) {
            user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(),
                            password == null ? "" : password))
                    .addOnCompleteListener(login -> {
                        if (login.isSuccessful()) {
                            deleteUserThenData(user, callback);
                        } else {
                            callback.onResult(false, message(login.getException()));
                        }
                    });
        } else {
            deleteUserThenData(user, callback);
        }
    }

    // Sign-in is deleted first: if Firebase refuses (needs a fresh login), the profile is kept
    private void deleteUserThenData(FirebaseUser user, ResultCallback callback) {
        String uid = user.getUid();
        user.delete().addOnCompleteListener(delete -> {
            if (delete.isSuccessful()) {
                usersRef.child(uid).removeValue().addOnCompleteListener(data -> {
                    auth.signOut();
                    callback.onResult(true, null);
                });
            } else if (delete.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                callback.onResult(false, ERROR_RELOGIN);
            } else {
                callback.onResult(false, message(delete.getException()));
            }
        });
    }

    private boolean hasProvider(String providerId) {
        FirebaseUser user = getUser();
        if (user == null) return false;
        for (UserInfo info : user.getProviderData()) {
            if (providerId.equals(info.getProviderId())) return true;
        }
        return false;
    }

    @Nullable
    private static String message(@Nullable Exception e) {
        return e == null ? null : e.getMessage();
    }
}
