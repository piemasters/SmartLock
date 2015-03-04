package net.davidnorton.securityapp.ui;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.runner.AndroidJUnit4;

import net.davidnorton.securityapp.ActivityRule;
import net.davidnorton.securityapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.contrib.DrawerActions.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);

    @Test
    public void testProfileTitleDisplayed() throws Exception {
        onView(withId(R.id.profiles_title));
    }

    @Test
    public void testProfileCardDisplayed() throws Exception {
        onView(withId(R.id.card_1)).perform(click());
        onView(withText(startsWith("Select"))).check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void testNavigationDrawerExists() throws Exception {
        openDrawer(R.id.drawer_layout);
    }


}