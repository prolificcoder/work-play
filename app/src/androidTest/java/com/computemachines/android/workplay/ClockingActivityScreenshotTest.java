package com.computemachines.android.workplay;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

@RunWith(AndroidJUnit4.class)
public class ClockingActivityScreenshotTest {

    @Rule
    public ActivityTestRule<ClockingActivity> activityRule = new ActivityTestRule<ClockingActivity>(ClockingActivity.class);

    @Test
    public void takeScreenshot() {
        onView(isRoot());
        ScreenShotter.takeScreenshot("3", activityRule.getActivity());
    }
}
