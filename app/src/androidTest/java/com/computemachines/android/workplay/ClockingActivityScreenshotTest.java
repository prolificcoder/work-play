package com.computemachines.android.workplay;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by tparker on 5/9/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ClockingActivityScreenshotTest {

    @Rule
    public ActivityTestRule<ClockingActivity> activityRule = new ActivityTestRule<ClockingActivity>(ClockingActivity.class);

    @Test
    public void takeScreenshot() {
        ScreenShotter.takeScreenshot("main_screen_1", activityRule.getActivity());
        onView(withId(R.id.play_label)).perform(click());

        ScreenShotter.takeScreenshot("main_screen_2", activityRule.getActivity());
    }
}
