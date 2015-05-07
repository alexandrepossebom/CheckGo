package com.possebom.checkgo;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.EditText;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.internal.util.Checks.checkNotNull;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ApplicationTest {

    public static final String STRING_TO_BE_TYPED = "Espresso";
    public static final String NUMBER_TO_BE_TYPED = "1111222233334444";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private static Matcher<? super View> hasErrorText(String expectedError) {
        return new ErrorTextMatcher(expectedError);
    }

    @Test
    public void invalidCardName() {
        onView(withId(R.id.fab)).perform(click());

        onView(withText("New Card")).check(matches(isDisplayed()));

        onView(withId(R.id.editTextCardName)).perform(typeText("   "), closeSoftKeyboard());

        onView(withId(R.id.buttonDefaultPositive)).perform(click());

        onView(withId(R.id.editTextCardName)).check(matches(hasErrorText("Invalid card name")));
    }


    @Test
    public void invalidCardNumber() {
        onView(withId(R.id.fab)).perform(click());

        onView(withText("New Card")).check(matches(isDisplayed()));

        onView(withId(R.id.editTextCardName)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());

        onView(withId(R.id.editTextCardNumber)).perform(typeText("123"), closeSoftKeyboard());

        onView(withId(R.id.buttonDefaultPositive)).perform(click());

        onView(withId(R.id.editTextCardNumber)).check(matches(hasErrorText("Invalid card number")));
    }


    @Test
    public void validCard() {
        onView(withId(R.id.fab)).perform(click());

        onView(withText("New Card")).check(matches(isDisplayed()));

        onView(withId(R.id.editTextCardName)).perform(typeText(STRING_TO_BE_TYPED), closeSoftKeyboard());

        onView(withId(R.id.editTextCardNumber)).perform(typeText(NUMBER_TO_BE_TYPED), closeSoftKeyboard());

        onView(withId(R.id.buttonDefaultPositive)).perform(click());

        onView(withId(R.id.recyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.fab)).perform(click());

        onView(withId(android.R.id.button1)).perform(click());
    }

    private static class ErrorTextMatcher extends TypeSafeMatcher<View> {
        private final String expectedError;

        private ErrorTextMatcher(String expectedError) {
            this.expectedError = checkNotNull(expectedError);
        }

        @Override
        public boolean matchesSafely(View view) {
            if (!(view instanceof EditText)) {
                return false;
            }
            EditText editText = (EditText) view;
            return expectedError.equals(editText.getError());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with error: " + expectedError);
        }
    }


}