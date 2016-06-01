package net.davidnorton.securityapp.ui;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import net.davidnorton.securityapp.ActivityRule;
import net.davidnorton.securityapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.contrib.DrawerActions.openDrawer;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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