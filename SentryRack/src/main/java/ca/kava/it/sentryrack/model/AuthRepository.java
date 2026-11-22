// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.model;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public interface AuthCallback {
        void onSuccess(UserData userData);
        void onError(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public UserData getCurrentUser() {
        return UserData.fromFirebaseUser(firebaseAuth.getCurrentUser());
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        callback.onSuccess(UserData.fromFirebaseUser(firebaseAuth.getCurrentUser()));
                    } else {
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            callback.onError("USER NOT FOUND");
                        } else {
                            callback.onError("Unable to sign in. Please try again");
                        }
                    }
                });
    }

    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        callback.onSuccess(UserData.fromFirebaseUser(firebaseAuth.getCurrentUser()));
                    } else {
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthUserCollisionException) {
                            callback.onError("An account with this email already exists");
                        } else {
                            callback.onError("Unable to create account");
                        }
                    }
                });
    }

    public void loginWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                        callback.onSuccess(UserData.fromFirebaseUser(firebaseAuth.getCurrentUser()));
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
                        callback.onSuccess("If an account exists for this email, a password reset link has been sent");
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