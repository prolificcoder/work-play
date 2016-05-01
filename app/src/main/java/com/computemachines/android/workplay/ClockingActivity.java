package com.computemachines.android.workplay;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.GregorianCalendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ClockingActivity extends AppCompatActivity {
    private static final String TAG = "ClockingActivity";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler handler = new Handler();
    private View content;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
//    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
//            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };

    private enum Clock { Work, Play, Pause };
    private Clock activeClock;
    private long activeClockStart;
    private long totalWork, totalPlay;

    public void setGoalRatioDenom(long goalRatioDenom) {
        this.goalRatioDenom = goalRatioDenom;
    }

    public long getGoalRatioDenom() {
        return goalRatioDenom;
    }

    private long goalRatioDenom = 1;


    private TimeButton lower, upper;
    private Button pause, settings;

    private TextView stat, stat_hint;

    private final String TOTAL_WORK_TAG = "TOTAL_WORK_TAG";
    private final String TOTAL_PLAY_TAG = "TOTAL_PLAY_TAG";
    private final String ACTIVE_CLOCK_TAG_INDEX = "ACTIVE_CLOCK_TAG";
    private final String ACTIVE_CLOCK_START_TAG = "ACTIVE_CLOCK_START_TAG";
    private final String GOAL_RATIO_TAG = "GOAL_RATIO_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clocking);


        activeClock = Clock.Pause;
        lower = (TimeButton)findViewById(R.id.lower_button);
        lower.setLabel("Work");
        upper = (TimeButton)findViewById(R.id.upper_button);
        upper.setLabel("Play");
        pause = (Button)findViewById(R.id.pause_button);
        settings = (Button)findViewById(R.id.settings);
        stat = (TextView)findViewById(R.id.clocks_stat);
        stat_hint = (TextView)findViewById(R.id.stat_hint);

        upper.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                delayedHide(5000);
                setActiveClock(Clock.Play);
                pause.setBackgroundResource(android.R.drawable.ic_media_pause);
                handler.post(updateUI);
            }
        });
        lower.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                delayedHide(5000);
                setActiveClock(Clock.Work);
                pause.setBackgroundResource(android.R.drawable.ic_media_pause);
                handler.post(updateUI);
            }
        });
        pause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                delayedHide(5000);
                if(activeClock == Clock.Pause){
                    // change view back to pause
                    view.setBackgroundResource(android.R.drawable.ic_media_pause);
                    // clicked replay
                    restartClocks();
                }else {
                    // change view from pause to replay
                    view.setBackgroundResource(R.drawable.ic_replay);
                    setActiveClock(Clock.Pause);
                }
                handler.post(updateUI);
            }
        });
        settings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                RatioChooser ratio = new RatioChooser();
                ratio.show(getFragmentManager(), "dialog");
            }
        });

        mVisible = true;
//        mControlsView = findViewById(R.id.fullscreen_content_controls);
        content = findViewById(R.id.fullscreen_content);
//
//
//        // Set up the user interaction to manually show or hide the system UI.
//        content.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private boolean stopFlag = false;
    private Runnable updateUI = new Runnable(){
        @Override
        public void run(){
            long currentTime = new GregorianCalendar().getTimeInMillis();
            long activeTotalPlay = totalPlay;
            long activeTotalWork = totalWork;
            if(activeClock == Clock.Play){
                activeTotalPlay = totalPlay + currentTime-activeClockStart;
            }else if(activeClock == Clock.Work){
                activeTotalWork = totalWork + currentTime-activeClockStart;
            }

            if(activeClock == Clock.Work){
                upper.setEnabled(true);
                lower.setEnabled(false);
            }else if(activeClock == Clock.Play){
                upper.setEnabled(false);
                lower.setEnabled(true);
            }else if(activeClock == Clock.Pause){
                upper.setEnabled(true);
                lower.setEnabled(true);
            }

            upper.setText(ms2hms(activeTotalPlay));
            lower.setText(ms2hms(activeTotalWork));
            long playCredit = activeTotalWork - activeTotalPlay*goalRatioDenom;
            long workCredit = playCredit/goalRatioDenom;

            if(workCredit >= 0){
                stat_hint.setText("Play Credit:");
                stat.setText(ms2hms(workCredit));
            }else{
                stat_hint.setText("Work Debt:");
                stat.setText("-"+ms2hms(-playCredit));
            }

            if(!stopFlag){
                handler.postDelayed(updateUI, 100);
            }
        }
    };

    private void restartClocks(){
        totalPlay = 0;
        totalWork = 0;
    }

    private void setActiveClock(Clock clock){
        long currentTime = new GregorianCalendar().getTimeInMillis();
        if(activeClock == Clock.Pause){
            activeClockStart = currentTime;
        }
        long clockedTime = currentTime - activeClockStart;
        if(activeClock == Clock.Work){
            totalWork += clockedTime;
        }else if(activeClock == Clock.Play){
            totalPlay += clockedTime;
        }
        activeClock = clock;
        activeClockStart = currentTime;
    }

    private String ms2hms(long ms){
        long total_secs = (long)(((double)ms)/1000d);
        long mod_secs = total_secs % 60;
        long total_mins = (long)(((double)(total_secs-mod_secs))/60d);
        long mod_mins = total_mins % 60;
        long total_hours = (long)(((double)(total_mins-mod_mins))/60d);
        return String.format("%d:%02d:%02d", total_hours, mod_mins, mod_secs);
//        return String.format("%d", ms);
    }

    @Override
    protected void onResume() {
        super.onResume();

        stopFlag = false;


        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        totalWork = pref.getLong(TOTAL_WORK_TAG, 0);
        totalPlay = pref.getLong(TOTAL_PLAY_TAG, 0);

        activeClock = Clock.values()[(int)pref.getLong(ACTIVE_CLOCK_TAG_INDEX, 2)];

        Log.d(TAG, "pref.getLong(ACTIVE_CLOCK_TAG_INDEX, 2) <= " + pref.getLong(ACTIVE_CLOCK_TAG_INDEX, 2));
        Clock clockValues[] = Clock.values();

        activeClockStart = pref.getLong(ACTIVE_CLOCK_START_TAG, 0);
        goalRatioDenom = pref.getLong(GOAL_RATIO_TAG, 1);

        updateUI.run();
        delayedHide(5000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopFlag = true;
        handler.removeCallbacks(updateUI);



        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(TOTAL_WORK_TAG, totalWork);
        editor.putLong(TOTAL_PLAY_TAG, totalPlay);
        long activeClockOrdinal = activeClock.ordinal();
        editor.putLong(ACTIVE_CLOCK_TAG_INDEX, activeClockOrdinal);
        editor.putLong(ACTIVE_CLOCK_START_TAG, activeClockStart);
        editor.putLong(GOAL_RATIO_TAG, goalRatioDenom);
        editor.commit();

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
//        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        handler.removeCallbacks(mShowPart2Runnable);

        handler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        handler.removeCallbacks(mHidePart2Runnable);
        handler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    public void delayedHide(int delayMillis) {
        handler.removeCallbacks(mHideRunnable);
        handler.postDelayed(mHideRunnable, delayMillis);
    }
}
