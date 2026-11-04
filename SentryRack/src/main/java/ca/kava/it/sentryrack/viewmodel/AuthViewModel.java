// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ca.kava.it.sentryrack.model.AuthState;
import ca.kava.it.sentryrack.model.User;
import ca.kava.it.sentryrack.repository.AuthRepository;

/**
 * ViewModel exposing authentication states and actions to the View.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<String> resetEmailStatus = new MutableLiveData<>();

    public AuthViewModel() {
        this.repository = new AuthRepository();
    }

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<String> getResetEmailStatus() {
        return resetEmailStatus;
    }

    public User getCurrentUser() {
        return repository.getCurrentUser();
    }

    public void loginWithEmail(String email, String password) {
        authState.setValue(AuthState.loading());
        repository.loginWithEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authState.setValue(AuthState.success(user));
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void signUpWithEmail(String email, String password) {
        authState.setValue(AuthState.loading());
        repository.signUpWithEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authState.setValue(AuthState.success(user));
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        authState.setValue(AuthState.loading());
        repository.loginWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authState.setValue(AuthState.success(user));
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void sendPasswordResetEmail(String email) {
        repository.sendPasswordResetEmail(email, new AuthRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                resetEmailStatus.setValue(message);
            }

            @Override
            public void onError(String errorMessage) {
                resetEmailStatus.setValue(errorMessage);
            }
        });
    }

    public void logout() {
        repository.logout();
        authState.setValue(AuthState.idle());
    }
}