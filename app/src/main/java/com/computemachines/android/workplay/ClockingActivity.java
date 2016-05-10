package com.computemachines.android.workplay;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.NoSuchElementException;

/** The main activity. Bundles time working with time playing. */
public class ClockingActivity extends AppCompatActivity {
    private static final String TAG = "ClockingActivity";
    public static ClockingActivity singleton;

    final Handler handler = new Handler();

    View lower, upper;
    Button middle;
    TextView stat, stat_hint, play_timer, work_timer;

    private HourglassClockSet clocks = null;

    /** Post this to this.handler to update the UI */
    Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            play_timer.setText(Clock.formatTime(clocks.play.getTotalTime()));
            work_timer.setText(Clock.formatTime(clocks.work.getTotalTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClockingActivity.singleton = this;

        setContentView(R.layout.activity_clocking);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        lower = (View) findViewById(R.id.work_view);
        upper = (View) findViewById(R.id.play_view);
        middle = (Button) findViewById(R.id.pause_button);
        work_timer = (TextView) findViewById(R.id.work_timer);
        play_timer = (TextView) findViewById(R.id.play_timer);

        stat = (TextView) findViewById(R.id.clocks_stat);
        stat_hint = (TextView) findViewById(R.id.stat_hint);

        upper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clocks.setActiveClock(clocks.play);
            }
        });
        lower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clocks.setActiveClock(clocks.work);
            }
        });
        middle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clocks.pause.isActive()) {
                    clocks.restartClocks();
                } else {
                    clocks.setActiveClock(clocks.pause);
                }
            }
        });
    }

    /** Must trigger onActivate for a recovered active clock to normalize UI state .*/
    @Override
    public void onResume() {
        super.onResume();

        if(clocks == null) {
            clocks = attemptRestoreClocks();
        }
        handler.post(updateUI);
    }

    @Override
    public void onPause() {
        saveClocks(clocks);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        saveClocks(clocks);
        clocks.close();

        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }

    /** Sets clocks to new recovered 'HourglassClockSet'. Only call after Views initialized. */
    private HourglassClockSet attemptRestoreClocks() {
        Log.v(TAG, "attemptRestoreClocks");
        Gson gson = new Gson();
        HourglassClockSet clocks = null;
        try {
            FileInputStream in = openFileInput("clocks.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            clocks = new HourglassClockSet(
                    (Clock)gson.fromJson(reader, Clock.class),
                    (Clock)gson.fromJson(reader, Clock.class),
                    (Clock)gson.fromJson(reader, Clock.class)
            );
            Log.v("after fromJson", gson.toJson(clocks.work));
            clocks.registerMyDefaults(this);
            Log.v("after register", gson.toJson(clocks.work));
            try {
                clocks.getActiveClock().registeredListener.onActivate();
                clocks.getActiveClock().thread.resumeTicking();
                Log.v("after onActivate", gson.toJson(clocks.work));
            } catch(NoSuchElementException e) {
                Log.v(TAG, "No clocks active");
            }
            reader.endArray();
            reader.close();
        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
        }
        if(clocks == null) {
            clocks = new HourglassClockSet();
            clocks.registerMyDefaults(this);
        }
        return clocks;
    }
    /** Saves clock state. Call 'attemptRestoreClocks()' to recover */
    private void saveClocks(HourglassClockSet clocks) {
        Log.v(TAG, "saveClocks");
        Gson gson = new Gson();
        String json = gson.toJson(clocks);
        Log.v(TAG, json);
        try {
//            deleteFile("clocks.json");
            FileOutputStream out = openFileOutput("clocks.json", Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.beginArray();
            gson.toJson(clocks.work, Clock.class, writer);
            gson.toJson(clocks.play, Clock.class, writer);
            gson.toJson(clocks.pause, Clock.class, writer);
            writer.endArray();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

