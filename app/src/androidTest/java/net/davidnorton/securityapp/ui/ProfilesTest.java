package net.davidnorton.securityapp.ui;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import net.davidnorton.securityapp.ActivityRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ProfilesTest extends TestCase {

    @Rule
    public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);

    @Test
    public void testApplyHome() throws Exception {

        onView(withText("Home")).perform(click());
    }

}