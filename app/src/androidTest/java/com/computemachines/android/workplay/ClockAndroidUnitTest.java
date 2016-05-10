package com.computemachines.android.workplay;

import android.support.test.runner.AndroidJUnit4;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

@RunWith(AndroidJUnit4.class)
//@SmallTest
public class ClockAndroidUnitTest {
    public static final String TAG = "ClockAndroidUnitTest";
    public int ticks;
    Gson gson = new Gson();
    Clock clock = new Clock();

    @Before
    public void default_clock() {
        clock.register(new Clock.TickListener() {
            @Override
            public void onTick() {
                ticks ++;
            }
        });
        ticks = 0;
        clock.setActive(true);
    }
    @Test
    public void to_from_json_identity() throws Exception {
        while(ticks == 0);
        String json = gson.toJson(clock);
        assertThat(gson.toJson(gson.fromJson(json, Clock.class)), is(json));
    }
    @Test
    public void total_time_after_tick() throws Exception {
        while(ticks < 2);
        assertThat(clock.getTotalTime(), greaterThan(0l));
    }
    @Test
    public void clock_fromJson_ticks() throws Exception {
        assertThat(ticks, is(0));
        Clock fromJson = gson.fromJson(gson.toJson(clock), Clock.class);
        fromJson.register(clock.thread.registeredTickListener);
        long start = fromJson.getTotalTime();
        fromJson.setActive(true);
        while(ticks < 2);
        assertThat(ticks, is(greaterThan(1)));
    }
}