// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC

package ca.kava.it.sentryrack.controller;

import ca.kava.it.sentryrack.model.AuthRepository;
import ca.kava.it.sentryrack.model.User;

public class AuthController {
    public interface Callback { void onLoading(); void onSuccess(User user); void onError(String errorMessage); void onActionMessage(String message); }
    private final AuthRepository repository; private final Callback callback;
    public AuthController() { this(null); }
    public AuthController(Callback callback) { this.repository = new AuthRepository(); this.callback = callback; }
    public User getCurrentUser() { return repository.getCurrentUser(); }
    public void loginWithEmail(String email, String password) { if (callback != null) callback.onLoading(); repository.loginWithEmail(email, password, new AuthRepository.AuthCallback() { @Override public void onSuccess(User user) { if (callback != null) callback.onSuccess(user); }
        @Override
        public void onError(String errorMessage) {
            if (callback != null) callback.onError(errorMessage);
        }
    });
    }
    public void signUpWithEmail(String email, String password) { if (callback != null) callback.onLoading(); repository.signUpWithEmail(email, password, new AuthRepository.AuthCallback() { @Override public void onSuccess(User user) { if (callback != null) callback.onSuccess(user); }
        @Override
        public void onError(String errorMessage) {
            if (callback != null) callback.onError(errorMessage);
        }
    });
    }
    public void loginWithGoogle(String idToken) { if (callback != null) callback.onLoading(); repository.loginWithGoogle(idToken, new AuthRepository.AuthCallback() { @Override public void onSuccess(User user) { if (callback != null) callback.onSuccess(user); }
        @Override
        public void onError(String errorMessage) {
            if (callback != null) callback.onError(errorMessage);
        }
    });
    }
    public void sendPasswordResetEmail(String email) { repository.sendPasswordResetEmail(email, new AuthRepository.ActionCallback() { @Override public void onSuccess(String message) { if (callback != null) callback.onActionMessage(message); }
        @Override
        public void onError(String errorMessage) {
            if (callback != null) callback.onError(errorMessage);
        }
    });
    }
    public void logout() { repository.logout(); } }