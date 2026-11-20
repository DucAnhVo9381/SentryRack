package ca.kava.it.sentryrack;

import android.view.WindowManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import ca.kava.it.sentryrack.view.SignUpActivity;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SignUpActivityUiTest {

    @Test
    public void signUpScreen_displaysAllRegistrationFieldsAndActions() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            onView(withId(R.id.registerNameInput)).check(matches(isDisplayed()));
            onView(withId(R.id.registerPhoneInput)).check(matches(isDisplayed()));
            onView(withId(R.id.registerEmailInput)).check(matches(isDisplayed()));
            onView(withId(R.id.registerPasswordInput)).check(matches(isDisplayed()));
            onView(withId(R.id.registerConfirmInput)).check(matches(isDisplayed()));
            onView(withId(R.id.registerAgreeCheck)).check(matches(isDisplayed()));
            onView(withId(R.id.registerPrivacyButton)).check(matches(isDisplayed()));
            onView(withId(R.id.registerButton)).check(matches(isDisplayed()));
            onView(withId(R.id.signupSignInButton)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingEmptyForm_displaysNameRequiredError() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            submitRegistrationForm();

            onView(withText("Name is required")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingFormWithoutEmail_displaysEmailRequiredError() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            enterText(R.id.registerNameInput, "Test User");
            submitRegistrationForm();

            onView(withText("Email is required")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingFormWithoutPassword_displaysPasswordRequiredError() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            enterText(R.id.registerNameInput, "Test User");
            enterText(R.id.registerEmailInput, "test@example.com");
            submitRegistrationForm();

            onView(withText("Password is required")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void submittingFormWithDifferentPasswords_displaysMismatchError() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            enterValidRegistrationDetails("first-password", "different-password");
            submitRegistrationForm();

            onView(withText("Passwords do not match")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void registrationRequiresPrivacyPolicyAcceptance() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            enterValidRegistrationDetails("matching-password", "matching-password");
            submitRegistrationForm();

            onView(withText("Please accept the privacy policy"))
                    .inRoot(new ToastRootMatcher())
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void privacyPolicyCheckbox_canBeToggled() {
        try (ActivityScenario<SignUpActivity> ignored =
                     ActivityScenario.launch(SignUpActivity.class)) {
            onView(withId(R.id.registerAgreeCheck))
                    .perform(scrollTo(), click())
                    .check(matches(isChecked()));
        }
    }

    @Test
    public void signInButton_closesSignUpScreen() {
        try (ActivityScenario<SignUpActivity> scenario =
                     ActivityScenario.launch(SignUpActivity.class)) {
            onView(withId(R.id.signupSignInButton)).perform(scrollTo(), click());

            scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
        }
    }

    private void enterValidRegistrationDetails(String password, String confirmation) {
        enterText(R.id.registerNameInput, "Test User");
        enterText(R.id.registerEmailInput, "test@example.com");
        enterText(R.id.registerPasswordInput, password);
        enterText(R.id.registerConfirmInput, confirmation);
    }

    private void enterText(int viewId, String text) {
        onView(withId(viewId)).perform(scrollTo(), replaceText(text), closeSoftKeyboard());
    }

    private void submitRegistrationForm() {
        onView(withId(R.id.registerButton)).perform(scrollTo(), click());
    }

    private static class ToastRootMatcher extends TypeSafeMatcher<Root> {
        @Override
        protected boolean matchesSafely(Root root) {
            return root.getWindowLayoutParams().get().type
                    == WindowManager.LayoutParams.TYPE_TOAST;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is a Toast window");
        }
    }
}
