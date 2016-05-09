package com.computemachines.android.workplay;

import com.google.common.collect.ImmutableList;

/** The default set of clocks for this application. */
class HourglassClockSet extends ClockSet {
    final Clock work, play, pause;

    HourglassClockSet() {
        work = new Clock();
        play = new Clock();
        pause = new Clock();
        clocks = ImmutableList.of(work, play, pause);
    }

    public HourglassClockSet(Clock work, Clock play, Clock pause) {
        this.work = work;
        this.play = play;
        this.pause = pause;
        clocks = ImmutableList.of(work, play, pause);
    }

    /** Registers 'ActivationListener' and 'TickListener' callbacks.
     *  Must be called after 'ClockingActivity' view members and handler is instantiated. */
    public void registerMyDefaults(final ClockingActivity activity) {
        play.register(new ActivationListener() {
            @Override
            public void onActivate() {
                activity.upper.setEnabled(false);
            }

            @Override
            public void onDeactivate() {
                activity.upper.setEnabled(true);
            }
        });
        work.register(new ActivationListener() {
            @Override
            public void onActivate() {
                activity.lower.setEnabled(false);
            }

            @Override
            public void onDeactivate() {
                activity.lower.setEnabled(true);
            }
        });
        pause.register(new ActivationListener() { // middle button is never disabled
            @Override
            public void onActivate() {
                activity.middle.setBackgroundResource(R.drawable.ic_replay);
            }

            @Override
            public void onDeactivate() {
                activity.middle.setBackgroundResource(android.R.drawable.ic_media_pause);
            }
        });
        register(new Clock.TickListener() {
            @Override
            public void onTick() {
                activity.handler.post(activity.updateUI);
            }
        });
    }

}
