package com.example.utapair;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/* This file is about SelectLevelActivity
 * it keep all method that use
 * in Select level of game page */
public class SelectLevelActivity extends AppCompatActivity {

    private Button buttonPlayEasy;   /* dimension =  2 rows , 3 cols */
    private Button buttonPlayNormal; /* dimension = 4 rows, 3 cols */
    private Button buttonPlayHard;   /* dimension = 6 rows, 3 cols */
    private ImageButton buttonBack;
    private TextToSpeech textToSpeech;
    private SoundClick soundClick;
    private int tapCount = 0;
    private boolean accessibilityMode;

    @Override
    /* this part will run when create this Activity */
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_select_level); /* menu page file layout */
        /* set mode from share preference */
        accessibilityMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ACCESSIBILITY_MODE",false);

        soundClick = new SoundClick(this);
        /* create object textToSpeak and set the language */
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                /* if init success set language in US */
                if (i != TextToSpeech.ERROR)
                    textToSpeech.setLanguage(Locale.US);
            }
        });

        /* set buttonPlayEasy */
        buttonPlayEasy = (Button) findViewById(R.id.easy_btn);
        buttonPlayEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click button start game easy mode */
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                /* use method follow AccessibilityMode */
                openEasyGameActivity();
            }
        });

        /* set buttonPlayNormal */
        buttonPlayNormal = (Button) findViewById(R.id.normal_btn);
        buttonPlayNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click button start game normal mode */
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                /* use method follow AccessibilityMode */
                openNormalGameActivity();
            }
        });

        /* set buttonPlayHard */
        buttonPlayHard = (Button) findViewById(R.id.hard_btn);
        buttonPlayHard.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click button start game hard mode */
            public void onClick(View v) {
                soundClick.playSoundClick(); /* sound click */
                /* use method follow AccessibilityMode */
                openHardGameActivity();
            }
        });

        /* set buttonBack */
        buttonBack = findViewById(R.id.backward_btn);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click button go to previous activity */
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                /* use NewIntent.openNextActivity to create Intent and start Activity follow AccessibilityMode and pass argument that need */
                NewIntent.openNextActivity(MainActivity.class,SelectLevelActivity.this,textToSpeech,"double tap to go back",500,accessibilityMode);
            }
        });

    }

    /* this part will run when this Activity start */
    protected void onStart() {
        super.onStart();
        /* if AccessibilityMode on when this activity start play sound */
        if(accessibilityMode) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String text = "Select level";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }, 500);
        }
    }

    /* this part is about when exit this activity */
    protected void onDestroy() {
        /* when destroy shutdown and turn off everything */
        super.onDestroy();
        textToSpeech.shutdown();
        soundClick.stopMediaPlayer();
        soundClick.releaseMediaPlayer();
    }

    /* method to start GameActivity easy mode */
    public void openEasyGameActivity(){
        /* set data mode layout_id and grid_id to GameActivity with putExtra */
        Map Map = new HashMap<String,Integer>();
        Map.put("MODE", -1);
        Map.put("LAYOUT_ID", R.layout.activity_game_easy2);
        Map.put("GRID_ID",R.id.GridLayout_easy);
        NewIntent.openNextActivity(GameActivity.class,SelectLevelActivity.this,Map,textToSpeech,"double tap to play level easy",500,accessibilityMode);
    }

    /* method to start GameActivity normal mode */
    public void openNormalGameActivity(){
        /* set data mode layout_id and grid_id to GameActivity with putExtra */
        Map Map = new HashMap<String,Integer>();
        Map.put("MODE", 0);
        Map.put("LAYOUT_ID", R.layout.activity_game_normal);
        Map.put("GRID_ID",R.id.GridLayout_meduim);
        NewIntent.openNextActivity(GameActivity.class,SelectLevelActivity.this,Map,textToSpeech,"double tap to play level Normal",500,accessibilityMode);
    }


    /* method to start GameActivity hard mode */
    public void openHardGameActivity(){
        /* set data mode layout_id and grid_id to GameActivity with putExtra */
        Map Map = new HashMap<String,Integer>();
        Map.put("MODE", 1);
        Map.put("LAYOUT_ID", R.layout.activity_game_hard);
        Map.put("GRID_ID",R.id.GridLayout_hard);
        NewIntent.openNextActivity(GameActivity.class,SelectLevelActivity.this,Map,textToSpeech,"double tap to play level hard",500,accessibilityMode);
    }

    /* method when pres back button */
    @Override
    public void onBackPressed() {
        NewIntent.launchActivity(MainActivity.class, this);
    }



}