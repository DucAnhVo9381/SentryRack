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

            onView(withText(R.string.error_email_empty)).check(matches(isDisplayed()));
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

            onView(withText(R.string.error_email_empty)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingEmptyForm_displaysPasswordRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText(R.string.error_password_empty)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingWithoutEmail_displaysEmailRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.passwordInput)).perform(typeText("Admin101!"), closeSoftKeyboard());
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText(R.string.error_email_empty)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingWithoutPassword_displaysPasswordRequiredError() {
        try (ActivityScenario<LoginActivity> ignored = launchLogin()) {
            onView(withId(R.id.emailInput)).perform(typeText("aaa@bbb.com"), closeSoftKeyboard());
            onView(withId(R.id.loginButton)).perform(scrollTo(), click());

            onView(withText(R.string.error_password_empty)).check(matches(isDisplayed()));
        }
    }


}
