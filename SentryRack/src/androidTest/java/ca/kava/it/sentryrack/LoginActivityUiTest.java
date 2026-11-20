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
}
