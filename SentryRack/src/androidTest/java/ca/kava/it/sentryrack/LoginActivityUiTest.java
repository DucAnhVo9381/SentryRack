package ca.kava.it.sentryrack;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import ca.kava.it.sentryrack.view.LoginActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import android.widget.EditText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import android.widget.CheckBox;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LoginActivityUiTest {

    @Test
    public void forgotPasswordWithoutEmail_displaysEmailRequiredError() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                LoginActivity.class
        ).putExtra(LoginActivity.FROM_LOGOUT, true);

        try (ActivityScenario<LoginActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.forgotPasswordButton)).perform(click());

            onView(withText("Email is required.")).check(matches(isDisplayed()));
        }
    }

    // Opens the login screen without auto-login (same setup as the test above)
    private ActivityScenario<LoginActivity> launchLogin() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                LoginActivity.class
        ).putExtra(LoginActivity.FROM_LOGOUT, true);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void loginScreen_displaysAllFieldsAndButtons() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
            onView(withId(R.id.passwordInput)).check(matches(isDisplayed()));
            onView(withId(R.id.rememberMeCheck)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.forgotPasswordButton)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.loginButton)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.googleButton)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.signUpButton)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingEmptyForm_displaysEmailRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText("Email is required.")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingEmptyForm_displaysPasswordRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText("Password is required.")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingWithoutEmail_displaysEmailRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.passwordInput)).perform(typeText("Admin101!"), closeSoftKeyboard());
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText("Email is required.")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingWithoutPassword_displaysPasswordRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.emailInput)).perform(typeText("aaa@bbb.com"), closeSoftKeyboard());
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText("Password is required.")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void rememberMeCheckbox_canBeToggled() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.rememberMeCheck)).perform(scrollTo()).check(matches(isNotChecked()));
            onView(withId(R.id.rememberMeCheck)).perform(click()).check(matches(isChecked()));
            onView(withId(R.id.rememberMeCheck)).perform(click()).check(matches(isNotChecked()));
        }
    }

    @Test
    public void signUpButton_opensSignUpScreen() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.signUpButton)).perform(scrollTo(), click());

            onView(withId(R.id.registerCard)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void loginFields_startEmpty() {
        try (ActivityScenario<LoginActivity> scenario = launchLogin()) {
            scenario.onActivity(activity -> {
                EditText email = activity.findViewById(R.id.emailInput);
                EditText password = activity.findViewById(R.id.passwordInput);

                assertNotNull(email);
                assertNotNull(password);
                assertEquals("", email.getText().toString());
                assertEquals("", password.getText().toString());
            });
        }
    }

    @Test
    public void loginScreen_buttonsEnabledAndRememberMeUnchecked() {
        try (ActivityScenario<LoginActivity> scenario = launchLogin()) {
            scenario.onActivity(activity -> {
                CheckBox rememberMe = activity.findViewById(R.id.rememberMeCheck);

                assertTrue(activity.findViewById(R.id.loginButton).isEnabled());
                assertTrue(activity.findViewById(R.id.googleButton).isEnabled());
                assertTrue(activity.findViewById(R.id.signUpButton).isEnabled());
                assertFalse(rememberMe.isChecked());
            });
        }
    }
}
