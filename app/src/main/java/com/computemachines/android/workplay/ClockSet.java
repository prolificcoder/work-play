package com.computemachines.android.workplay;

import android.util.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.NoSuchElementException;

/**
 * Created by tparker on 5/1/16.
 */
public class ClockSet {
    public final String TAG = "ClockSet";
    public ImmutableList<Clock> clocks;
    // Holds a collection of Clocks with exactly one active once started.

    public void restartClocks() {
        for(Clock clock: clocks) {
            clock.reset();
            clock.setActive(false);
        }
    }
    public void register(Clock.TickListener listener) {
        for(Clock clock: clocks) {
            clock.register(listener);
        }
    }
    public void setActiveClock(Clock newActiveClock) {
        try {
            getActiveClock().setActive(false);
        } catch(NoSuchElementException e){
            Log.v(TAG, "setting first clock active");
        }
        assert Iterables.contains(clocks, newActiveClock);
        newActiveClock.setActive(true);
    }
    public Clock getActiveClock() {
        return Iterables.find(clocks,
                new Predicate<Clock>(){
                    public boolean apply(Clock clock){
                        return clock.isActive();
                    }
                });
    }
    public void close() {
        for(Clock clock : clocks) {
            clock.close();
        }
    }
}
