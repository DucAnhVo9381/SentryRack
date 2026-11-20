// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.controller;

import ca.kava.it.sentryrack.model.AuthRepository;
import ca.kava.it.sentryrack.model.UserData;
import ca.kava.it.sentryrack.model.UserRepository;

public class AuthController {

    public interface Callback {
        void onLoading();
        void onSuccess(UserData userData);
        void onError(String errorMessage);
        void onActionMessage(String message);
    }

    private final AuthRepository repository;
    private final UserRepository userRepository;
    private final Callback callback;

    public AuthController() {
        this(null);
    }

    public AuthController(Callback callback) {
        this.repository = new AuthRepository();
        this.userRepository = new UserRepository();
        this.callback = callback;
    }

    public UserData getCurrentUser() {
        return repository.getCurrentUser();
    }

    public void loginWithEmail(String email, String password) {
        if (callback != null) callback.onLoading();
        repository.loginWithEmail(email, password, authCallback());
    }

    public void signUpWithEmail(String email, String password) {
        if (callback != null) callback.onLoading();
        repository.signUpWithEmail(email, password, authCallback());
    }

    public void loginWithGoogle(String idToken) {
        if (callback != null) callback.onLoading();
        repository.loginWithGoogle(idToken, authCallback());
    }

    public void sendPasswordResetEmail(String email) {
        repository.sendPasswordResetEmail(email, new AuthRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                if (callback != null) callback.onActionMessage(message);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    public void logout() {
        repository.logout();
    }

    // Shared by email login, sign-up and Google sign-in
    private AuthRepository.AuthCallback authCallback() {
        return new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(UserData userData) {
                userRepository.createOrUpdateOnLogin(userData);
                if (callback != null) callback.onSuccess(userData);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
            }
        };
    }
}