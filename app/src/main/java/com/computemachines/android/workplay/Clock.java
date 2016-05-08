package com.computemachines.android.workplay;

import android.os.Handler;
import android.os.Looper;

import java.util.GregorianCalendar;

/**
 *  Serialize with Gson#toJson
 *  After deserialization must re-register all callbacks
 */
public class Clock {
    public interface TickListener {
        public void onTick();
    }

    public static String formatTime(long time) {
        long total_secs = (long) (((double) time) / 1000d);
        long mod_secs = total_secs % 60;
        long total_mins = (long) (((double) (total_secs - mod_secs)) / 60d);
        long mod_mins = total_mins % 60;
        long total_hours = (long) (((double) (total_mins - mod_mins)) / 60d);
        return String.format("%d:%02d:%02d", total_hours, mod_mins, mod_secs);
    }
    public static long getCurrentTime() {
        return new GregorianCalendar().getTimeInMillis();
    }

    // should contain a single accumulating timer thread
    // time accumulates when active
    // ActivationListener must be registered before calling setActive.

    private boolean isActive = false;

    private long totalTime;
    public long getTotalTime() {
        return totalTime;
    }

    private long lastTick;

    class ClockThread extends Thread implements ActivationListener {
        public Handler handler;
        public TickListener registeredTickListener = new TickListener() {
            @Override
            public void onTick() {

            }
        };

        // TODO
        // Increment total time by difference since lastTick

        // currently increments totalTime by constant 1
        public final Runnable tick = new Runnable() {
            @Override
            public void run() {
                long currentTime = Clock.getCurrentTime();
                totalTime += (currentTime-lastTick);
                lastTick = currentTime;
                registeredTickListener.onTick();

                // suggest tick at the next whole second. do not expect
                if(isActive) {
                    long mod = currentTime%1000;
//                    handler.postAtTime(this, currentTime + 1000 - );
                    handler.postDelayed(this, 1000 - (currentTime % 1000));
                }
            }
        };

        @Override
        public void run() {
            // TODO
            // while clock is active, trigger all clock render every second
            // when clock goes inactive, add exact remainder, then pause

            Looper.prepare();
            handler = new Handler();
            Looper.loop();
        }

        @Override
        public void onActivate() {
            handler.removeCallbacks(tick);
            lastTick = Clock.getCurrentTime();
            handler.post(tick);
        }

        @Override
        public void onDeactivate() {
            handler.removeCallbacks(tick);
            handler.post(tick);
        }

    };
    transient ClockThread thread;


    public Clock() {
        thread = new ClockThread();
        thread.start();
        while(thread.handler == null); // there should be a better way to wait for handler init
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
//        assert registeredListener != null;
        if(!isActive && active) {
            isActive = true;
            thread.onActivate();
            registeredListener.onActivate();
        } else if(isActive && !active) {
            isActive = false;
            thread.onDeactivate();
            registeredListener.onDeactivate();
        }
        isActive = active;
    }
    public void reset() {
        totalTime = 0;
        lastTick = getCurrentTime();
    }

    transient ActivationListener registeredListener = new ActivationListener() {
        @Override
        public void onActivate() {

        }

        @Override
        public void onDeactivate() {

        }
    };
    public void register(ActivationListener listener) {
        registeredListener = listener;
    }

    // Dont call more than on the same clock.
    public void register(TickListener listener) {
        thread.registeredTickListener = listener;
    }

    public void close() {
        if(thread.handler != null)
            thread.handler.getLooper().quit();
    }
}
