package com.example.utapair;

import static com.example.utapair.R.raw.sc;
import static java.lang.String.valueOf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/* this class is about ScoreboardActivity
* that show all score in database
* follow level and mode */
public class ScoreboardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private ArrayList<ScoreboardUser> scoreboardUserList;
    private RecyclerView recyclerView;
    private ImageButton buttonProfile;
    private ImageButton buttonSetting;
    private ImageButton buttonHome;
    private CheckBox buttonCheckbox;
    private String saveName,buttonLevel,textLevel,score;
    private TextToSpeech textToSpeech;
    private int tapCount = 0;
    private int sdCount = 0;
    private SoundClick soundClick;
    private boolean accessibilityMode;
    private String scoreboardURL = "https://uta-pair-api.herokuapp.com/scoreboard.php";
    private String bestPlaceURL = "https://uta-pair-api.herokuapp.com/scoreboardShowBestScore.php";
    SharedPreferences sh;
    @Override
    /* this part will run when create this Activity */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard); /* set layout */

        /* set mode from share preference */
        accessibilityMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("ACCESSIBILITY_MODE",false);
        soundClick = new SoundClick(this);
        /* set spinner for select level */
        Spinner spinner = findViewById(R.id.level_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.level,R.layout.spinner_text_select);
        adapter.setDropDownViewResource(R.layout.spinner_text_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        sh = getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
        saveName = sh.getString("SAVED_NAME","");
        /* set checkbox for BlindMode */
        buttonCheckbox = findViewById(R.id.blind_mode_checkbox);
        buttonCheckbox.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("BLIND_SCOREBOARD",false));
        buttonCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                /* use method follow AccessibilityMode */
                if(accessibilityMode){
                    String text;
                    if (buttonCheckbox.isChecked()) {
                        text = "Checked mode blind";

                    }
                    else {
                        text = "Checked off mode blind";
                    }
                    textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                }
                switch(buttonCheckbox.getId()) {
                    case R.id.blind_mode_checkbox:
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                .putBoolean("BLIND_SCOREBOARD", buttonCheckbox.isChecked()).commit();
                        break;
                }
                showScore();
            }
        });


        /* for keep data and show in recyclerView */
        recyclerView = findViewById(R.id.scoreboard_recycler_view);

        /* create Arraylist fot keep data in recyclerView */
        scoreboardUserList = new ArrayList<>();

        /* create object textToSpeak and set the language */
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                /* if init success set language in US */
                if (i != TextToSpeech.ERROR)
                    textToSpeech.setLanguage(Locale.US);
            }
        });

        /* set buttonProfile */
        buttonProfile = (ImageButton) findViewById(R.id.profile_btn);
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click buttonProfile start AccountActivity */
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                if(sh.contains("SAVED_NAME")){  /* if login go to profile otherwise go to account */
                    /* use NewIntent.openNextActivity to create Intent and start Activity follow AccessibilityMode and pass argument that need */
                    NewIntent.openNextActivity(ProfileActivity.class,ScoreboardActivity.this,textToSpeech,"double tap to go to Profile",500,accessibilityMode);
                }
                else{
                    /* use NewIntent.openNextActivity to create Intent and start Activity follow AccessibilityMode and pass argument that need */
                    NewIntent.openNextActivity(AccountActivity.class,ScoreboardActivity.this,textToSpeech,"double tap to go to Account",500,accessibilityMode);
                }

            }
        });

        /* set buttonSetting */
        buttonSetting = (ImageButton) findViewById(R.id.setting_btn);
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click buttonSetting start SettingActivity */
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                /* use NewIntent.openNextActivity to create Intent and start Activity follow AccessibilityMode and pass argument that need */
                NewIntent.openNextActivity(SettingActivity.class,ScoreboardActivity.this,textToSpeech,"double tap to go to setting",500,accessibilityMode);

            }
        });

        /* set buttonBack */
        buttonHome = findViewById(R.id.home_btn);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            /* set when click button go to previous activity */
            public void onClick(View view) {
                soundClick.playSoundClick(); /* sound click */
                /* use NewIntent.openNextActivity to create Intent and start Activity follow AccessibilityMode and pass argument that need */
                NewIntent.openNextActivity(MainActivity.class,ScoreboardActivity.this,textToSpeech,"double tap to go back",500,accessibilityMode);
            }
        });
    }
    @Override
    public void onBackPressed() {
        NewIntent.launchActivity(MainActivity.class, this);
    }

    /* this part will run when this Activity start */
    protected void onStart() {
        super.onStart();
        sdCount = 0 ;
        /* if AccessibilityMode on when this activity start play sound */
        if(accessibilityMode) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String text = "Scoreboard";
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    /* let textToSpeech speak 2 time because if speak all word in one time it to fast and hard to understand */
                    text = "level easy";
                    textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
                    /* if on blind mode say it to */
                    if(PreferenceManager.getDefaultSharedPreferences(ScoreboardActivity.this).getBoolean("BLIND_SCOREBOARD",false)){
                        text = "on blind Mode";
                        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
                    }
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

    /* method to set data in layout */
    private void setAdapter() {
        /* set recyclerView and put data in it with layout */
        ScoreboardRecyclerAdapter scoreboardRecyclerAdapter = new ScoreboardRecyclerAdapter(scoreboardUserList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(scoreboardRecyclerAdapter);
    }

    public void showScore (){
        /* set button level follow mode and level to put this data in to database */
        if(buttonCheckbox.isChecked()){
            if(textLevel.equals("EASY")){
                buttonLevel="MAL01";
            }
            else if(textLevel.equals("NORMAL")){
                buttonLevel="MAL02";
            }
            else if(textLevel.equals("HARD")){
                buttonLevel="MAL03";
            }
        }
        else{
            if(textLevel.equals("EASY")){
                buttonLevel="MAL04";
            }
            else if(textLevel.equals("NORMAL")){
                buttonLevel="MAL05";
            }
            else if(textLevel.equals("HARD")){
                buttonLevel="MAL06";
            }
        }
        /* collect data from database */
        setUserInfo(buttonLevel);
    }

    /* method to add data from database */
    public void setUserInfo(String buttonLevel){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, scoreboardURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                /* If response from database is FAILURE */
                if(response.equals("FAILURE")){
                    scoreboardUserList.clear();     /* clear data */
                    setAdapter();       /* show in recyclerView */
                    Toast.makeText(ScoreboardActivity.this, "There is no record" , Toast.LENGTH_SHORT).show();
                    if(accessibilityMode){
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String text = "There is no record ";
                                textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
                            }
                        }, 500);
                    }
                }
                else {
                    try {
                        scoreboardUserList.clear();     /* clear data */
                        JSONArray products = new JSONArray(response);
                            for(int i=0;i<products.length();i++){   /* dor loop to collect data from database */
                                JSONObject productObject = products.getJSONObject(i);
                                String username = productObject.getString("username");
                                Integer endTime = productObject.getInt("endTime");
                                String minute = String.valueOf(endTime/6000);
                                String second = String.valueOf((endTime/100)%60);
                                String milliSecond = String.valueOf(endTime%100);
                                System.out.println(second.length());
                                /* If length of "second" less 2 (0:0:00) */
                                if(second.length()<2){
                                    /* If length of "milliSecond" less 2 (0:0:0) */
                                    if(milliSecond.length()<2){
                                        score = minute+":"+"0"+second+":"+"0"+milliSecond;
                                    }
                                    /* else length of "milliSecond" not less 2 (0:0:00) */
                                    else{
                                        score = minute+":"+"0"+second+":"+milliSecond;
                                    }
                                }
                                /* If length of "milliSecond" less 2 (0:00:0) */
                                else if(milliSecond.length()<2){
                                    /* If length of "second" less 2 (0:0:0) */
                                    if(second.length()<2){
                                        score = minute+":"+"0"+second+":"+"0"+milliSecond;
                                    }
                                    /* else length of "second" not less 2 (0:00:0) */
                                    else{
                                        score = minute+":"+second+":"+"0"+milliSecond;
                                    }
                                }
                                /* else length of "second" not less 2 (0:00:00) */
                                else{
                                    score = minute+":"+second+":"+milliSecond;
                                }
                                scoreboardUserList.add(new ScoreboardUser(i+1,username,score));
                                setAdapter();       /* show in recyclerView */
                            }
                        showBestPlace(buttonLevel); /* show best place user */
                    } catch (JSONException e) {
                        e.printStackTrace();    /* if JSON error */
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {    /* if error */
                Toast.makeText(ScoreboardActivity.this, "Server error. Please try again later", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            /* get data that use in database */
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("LEVEL", buttonLevel);     /* put level to database */
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    @Override
    /* when select level */
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        /* change level and show with toast */
        textLevel = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(adapterView.getContext(),textLevel,Toast.LENGTH_SHORT).show();
        /* count for do not speak when this activity is start */
        if(sdCount<=1)
            sdCount++;
        if (sdCount>1) {
            soundClick.playSoundClick();
            /* speak when AccessibilityMode on */
            if (accessibilityMode){
                String text = "Select level " + textLevel;
                textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                if(PreferenceManager.getDefaultSharedPreferences(ScoreboardActivity.this).getBoolean("BLIND_SCOREBOARD",false)){
                    text = "on Blind Mode";
                    textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
                }
            }

        }
        showScore();
    }
    /* method show best place user*/
    public void showBestPlace(String buttonLevel){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, bestPlaceURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                    try {
                        JSONArray products = new JSONArray(response);
                        for(int i=0;i<products.length();i++){   /* for loop to collect data from database */
                            JSONObject productObject = products.getJSONObject(i);
                            Integer row_index = productObject.getInt("row_index");
                            String text ="Congratulations! your best score is on "+ row_index + "th place.";
                            Toast.makeText(ScoreboardActivity.this,text , Toast.LENGTH_LONG).show();
                            /* If AccessibilityMode on speak and delay more than speak in method onStart */
                            if(accessibilityMode){
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
                                    }
                                }, 600);
                            }
                        }

                    } catch (JSONException e) {

                        String text = "There is no record of your score in the top 50.";
                        Toast.makeText(ScoreboardActivity.this,text , Toast.LENGTH_LONG).show();
                        if(accessibilityMode){
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
                                    String text = "Keep going !";
                                    textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);
                                }
                            }, 600);
                        }
                        e.printStackTrace();    /* if JSON error */
                    }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {    /* if error */
                Toast.makeText(ScoreboardActivity.this, "Server error. Please try again later", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            /* get data that use in database */
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("LEVEL", buttonLevel);     /* put level to database */
                data.put("USERNAME", saveName);      /* put username to database */
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    @Override
    /* implement because implement interface */
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private int checkLoginData(){
        sh = getSharedPreferences("MY_SHARED_PREF", Context.MODE_PRIVATE);
        if(sh.contains("SAVED_NAME")){
            return 1 ;
        }
        else{
            return 0;
        }

    }
}