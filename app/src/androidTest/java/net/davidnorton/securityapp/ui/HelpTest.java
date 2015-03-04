package net.davidnorton.securityapp.ui;


import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import junit.framework.TestCase;

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
import static android.support.test.espresso.contrib.DrawerMatchers.*;

@RunWith(AndroidJUnit4.class)
public class HelpTest extends TestCase {

    @Rule
    public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);

    @Test
    public void testOpenHelp() throws Exception {
        // Opens the navigation drawer.
        openDrawer(R.id.drawer_layout);
        // Scrolls down the navigation drawer.
        onView(withId(R.id.drawer_layout)).perform(swipeUp());
        // Selects the Help navigation item.
        onView(withText("Help")).perform(click());
    }

}