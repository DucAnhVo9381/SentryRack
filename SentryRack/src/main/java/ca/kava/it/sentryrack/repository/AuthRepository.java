// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.repository;

import ca.kava.it.sentryrack.model.User;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Repository performing Firebase Authentication API operations.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

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
    }

    public User getCurrentUser() {
        return User.fromFirebaseUser(firebaseAuth.getCurrentUser());
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        callback.onSuccess(User.fromFirebaseUser(firebaseAuth.getCurrentUser()));
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
                        callback.onSuccess(User.fromFirebaseUser(firebaseAuth.getCurrentUser()));
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
                        callback.onSuccess(User.fromFirebaseUser(firebaseAuth.getCurrentUser()));
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
}