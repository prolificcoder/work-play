package com.computemachines.android.workplay;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

public class ClockingActivity extends AppCompatActivity {
    private static final String TAG = "ClockingActivity";
    public static ClockingActivity singleton;

    final Handler handler = new Handler();

    TimeButton lower, upper;
    Button middle, settings;
    TextView stat, stat_hint;

    private MyClockSet clocks;

    Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            upper.setText(Clock.formatTime(clocks.play.getTotalTime()));
            lower.setText(Clock.formatTime(clocks.work.getTotalTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClockingActivity.singleton = this;

        setContentView(R.layout.activity_clocking);

        clocks = new MyClockSet();
        attemptRestoreClocks();
        clocks.registerMyDefaults(this);

        lower = (TimeButton) findViewById(R.id.lower_button);
        lower.setLabel("Work");
        upper = (TimeButton) findViewById(R.id.upper_button);
        upper.setLabel("Play");
        middle = (Button) findViewById(R.id.pause_button);

        settings = (Button) findViewById(R.id.settings);
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
//        settings.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                RatioChooser ratio = new RatioChooser();
//                ratio.show(getFragmentManager(), "dialog");
//            }
//        });
    }
    @Override
    public void onResume() {
        super.onResume();
        handler.post(updateUI);
        try {
            clocks.getActiveClock().registeredListener.onActivate();
            clocks.getActiveClock().thread.onActivate();
        } catch(NoSuchElementException e) {
            Log.v(TAG, "No clocks active");
        }
    }

    @Override
    public void onPause() {
        saveClocks();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        clocks.close();

        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }
    private void attemptRestoreClocks() {
        Log.v(TAG, "attemptRestoreClocks");
        Gson gson = new Gson();
        try {
            FileInputStream in = openFileInput("clocks.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            clocks = new MyClockSet(
                    (Clock)gson.fromJson(reader, Clock.class),
                    (Clock)gson.fromJson(reader, Clock.class),
                    (Clock)gson.fromJson(reader, Clock.class)
            );
            reader.endArray();
            reader.close();
        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
        }
    }
    private void saveClocks() {
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

class MyClockSet extends ClockSet {
    final Clock work, play, pause;

    MyClockSet() {
        work = new Clock();
        play = new Clock();
        pause = new Clock();
        clocks = ImmutableList.of(work, play, pause);
    }

    public MyClockSet(Clock work, Clock play, Clock pause) {
        this.work = work;
        this.play = play;
        this.pause = pause;
        clocks = ImmutableList.of(work, play, pause);
    }

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
