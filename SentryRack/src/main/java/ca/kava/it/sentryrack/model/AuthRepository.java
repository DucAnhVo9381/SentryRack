// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository performing Firebase Authentication and User node Sync operations.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference rootRef;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public User getCurrentUser() {
        return User.fromFirebaseUser(firebaseAuth.getCurrentUser());
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        User user = User.fromFirebaseUser(firebaseAuth.getCurrentUser());
                        syncUserLastLogin(user);
                        callback.onSuccess(user);
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                        callback.onError(err);
                    }
                });
    }

    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        User user = User.fromFirebaseUser(firebaseAuth.getCurrentUser());
                        registerUserNode(user);
                        callback.onSuccess(user);
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Sign up failed";
                        callback.onError(err);
                    }
                });
    }

    public void loginWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        User user = User.fromFirebaseUser(firebaseAuth.getCurrentUser());
                        syncUserLastLogin(user);
                        callback.onSuccess(user);
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Google sign-in failed";
                        callback.onError(err);
                    }
                });
    }

    public void sendPasswordResetEmail(String email, ActionCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Password reset link sent to " + email);
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        callback.onError(err);
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    private void registerUserNode(User user) {
        if (user == null || user.getUid() == null) return;
        Map<String, Object> childUpdates = new HashMap<>();
        String basePath = "/users/" + user.getUid() + "/";
        childUpdates.put(basePath + "name", user.getDisplayName());
        childUpdates.put(basePath + "email", user.getEmail());
        childUpdates.put(basePath + "createdAt", ServerValue.TIMESTAMP);
        childUpdates.put(basePath + "lastLoginAt", ServerValue.TIMESTAMP);
        rootRef.updateChildren(childUpdates);
    }

    private void syncUserLastLogin(User user) {
        if (user == null || user.getUid() == null) return;
        Map<String, Object> childUpdates = new HashMap<>();
        String basePath = "/users/" + user.getUid() + "/";
        childUpdates.put(basePath + "lastLoginAt", ServerValue.TIMESTAMP);
        childUpdates.put(basePath + "email", user.getEmail());
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            childUpdates.put(basePath + "name", user.getDisplayName());
        }
        rootRef.updateChildren(childUpdates);
    }
}