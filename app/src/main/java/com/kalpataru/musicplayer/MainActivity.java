package com.kalpataru.musicplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private Button mNext, mPause, mPrevious, mHome, mLooping, mAlarm, mShuffle;
    private TextView mTitle, mRefresh, mCurrent, mDuration;
    private SeekBar mSeekbar;
    private CardView mCardRefresh;
    private ImageView mImageView;
    static MediaPlayer myMediaPlayer;
    public int position = 0, albumSize;
    double cur =0.0, dur=0.0;
    private ListView mPlayerList;
    private String[] mPlayerItems;
    private boolean isLooping = false, isShuffled = false;
    private ArrayList<File> mySongs;
    private Thread updateSeekBar;
    //TimerBelow
    private LinearLayout mTimerInner, mTimerOuter;
    private EditText mEditTime;
    private CountDownTimer mCountDownTimer;
    private long startTimeInMillis;
    private TextView mTimer;
    private Boolean mTimerRuning = false;
    private Button mBtnStartPause, mBtnReset, mBtnCancel;
    private long mTimeLeftInMillis ;
    //TimerAbove
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //TimerBelow
        mEditTime = findViewById(R.id.activity_main_edit_text_timer);
        mBtnCancel = findViewById(R.id.activity_main_button_timer_cancel);
        mTimer = findViewById(R.id.activity_main_text_view_timer);
        mBtnStartPause = findViewById(R.id.activity_main_button_timer_start_pause);
        mBtnReset = findViewById(R.id.activity_main_button_timer_reset);
        mTimerInner = findViewById(R.id.activity_main_linear_layout_timer_inner);
        mTimerOuter = findViewById(R.id.activity_main_linear_layout_timer_outer);
        mBtnStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimerRuning){
                    pauseTimer();
                }else {
                    startTimer();
                }
            }
        });
        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tempBtnName = mBtnReset.getText().toString();
                if (tempBtnName.equals("RESET")){
                    mTimer.setText("00:00");
                    mBtnStartPause.setEnabled(false);
                    mBtnStartPause.setTextColor(Color.GRAY);
                    mBtnReset.setText("SET");
                    mTimerInner.setVisibility(View.VISIBLE);
                }else {
                    hideKeyBoard();
                    String temp = mEditTime.getText().toString().trim();
                    if (!temp.equals("")){
                        long longTemp = Long.valueOf(temp);
                        if (longTemp > 0){
                            resetTimer(longTemp * 60000);
                        }else {
                            Toast.makeText(MainActivity.this, "Value must be positive", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        resetTimer(10 * 60000);
                    }
                }
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimerOuter.setVisibility(View.GONE);
            }
        });
        updateCountDownText();
        //TimerAbove
        mShuffle = findViewById(R.id.activity_main_button_shuffle);
        mImageView = findViewById(R.id.activity_main_image_view);
        mCurrent = findViewById(R.id.activity_main_text_view_current);
        mDuration = findViewById(R.id.activity_main_text_view_duration);
        mRefresh = findViewById(R.id.activity_main_text_view_refresh);
        mCardRefresh = findViewById(R.id.activity_main_card_view_refresh);
        mNext = findViewById(R.id.activity_main_button_next);
        mPause = findViewById(R.id.activity_main_button_pause);
        mPrevious = findViewById(R.id.activity_main_button_previous);
        mTitle = findViewById(R.id.activity_main_text_view_title);
        mSeekbar = findViewById(R.id.activity_main_seek_bar);
        mHome = findViewById(R.id.activity_main_button_home);
        mAlarm = findViewById(R.id.activity_main_button_alarm);
        mLooping = findViewById(R.id.activity_main_button_looping);
        mPlayerList = findViewById(R.id.activity_main_list_view);
        mTitle.setSelected(true);
        if (myMediaPlayer != null){
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }
        try {
            displaySongs();
        }catch (Exception e){
            mCardRefresh.setVisibility(View.VISIBLE);
        }
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
        mPlayerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { play(i); }
        });
        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause();
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous();
            }
        });
        mLooping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loop();
            }
        });
        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffle();
            }
        });
        mAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimerOuter.setVisibility(View.VISIBLE);
            }
        });

    }

    public void runtimePermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    public ArrayList<File> findsong(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                arrayList.addAll(findsong(singleFile));
            }else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")){
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }
    void displaySongs(){
        mySongs = findsong(Environment.getExternalStorageDirectory());
        albumSize = mySongs.size();
        mPlayerItems = new String[mySongs.size()];
        for (int i = 0; i<mySongs.size(); i++){
            mPlayerItems[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mPlayerItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.rgb(255, 255, 255));
                return view;
            }
        };
        mPlayerList.setAdapter(myAdapter);
        play(0);
        pause();
    }
    private void play(int i) {
        try {
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mPause.setBackgroundResource(R.drawable.icon_pause);
        position = i;
        String songName = mPlayerList.getItemAtPosition(position).toString();
        mTitle.setText(songName);
        Uri uri5 = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri5);
        myMediaPlayer.start();
        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isShuffled){
                    Random random = new Random();
                    int newPos = random.nextInt(albumSize);
                    play(newPos);
                }else {
                    position = ((position + 1)%mySongs.size());
                    play(position);
                }
            }
        });

        mSeekbar.setMax(myMediaPlayer.getDuration());
        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuratioon = myMediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuratioon){
                    try {
                        sleep(500);
                        currentPosition = myMediaPlayer.getCurrentPosition();
                        mSeekbar.setProgress(currentPosition);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        updateSeekBar.start();
        mSeekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.MULTIPLY);
        mSeekbar.getThumb().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myMediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        cur = myMediaPlayer.getCurrentPosition();
        dur = myMediaPlayer.getDuration();
        mDuration.setText(String.format(Locale.getDefault(),"%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) dur), TimeUnit.MILLISECONDS.toSeconds((long) dur) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) dur))));
        mCurrent.setText(String.format(Locale.getDefault(),"%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) cur), TimeUnit.MILLISECONDS.toSeconds((long) cur) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) cur))));
        changeBackground();
    }
    private void pause(){
        mSeekbar.setMax(myMediaPlayer.getDuration());
        if (myMediaPlayer.isPlaying()){
            mPause.setBackgroundResource(R.drawable.icon_play);
            myMediaPlayer.pause();
        }else {
            mPause.setBackgroundResource(R.drawable.icon_pause);
            myMediaPlayer.start();
        }
    }
    private void next(){
        myMediaPlayer.stop();
        if (isShuffled){
            Random random = new Random();
            int newPos = random.nextInt(albumSize);
            play(newPos);
        }else {
            position = ((position + 1)%mySongs.size());
            play(position);
        }
    }
    private void previous(){
        myMediaPlayer.stop();
        if (isShuffled){
            Random random = new Random();
            int newPos = random.nextInt(albumSize);
            play(newPos);
        }else {
            position = ((position - 1)<0)?(mySongs.size()-1):(position-1);
            play(position);
        }
    }
    private void loop(){
        mPause.setBackgroundResource(R.drawable.icon_pause);
        if (isLooping){
            myMediaPlayer.setLooping(false);
            myMediaPlayer.start();
            Toast.makeText(MainActivity.this, "Looping disabled", Toast.LENGTH_SHORT).show();
            mLooping.setBackgroundResource(R.drawable.icon_repeat);
            isLooping =! isLooping;
        }else {
            myMediaPlayer.setLooping(true);
            myMediaPlayer.start();
            Toast.makeText(MainActivity.this, "Looping enabled", Toast.LENGTH_SHORT).show();
            mLooping.setBackgroundResource(R.drawable.icon_repeating);
            isLooping =! isLooping;
        }
    }
    private void shuffle() {
        if (isShuffled){
            isShuffled =! isShuffled;
            Toast.makeText(MainActivity.this, "Shuffle disabled", Toast.LENGTH_SHORT).show();
            mShuffle.setBackgroundResource(R.drawable.icon_random);
        }else {
            isShuffled =! isShuffled;
            Toast.makeText(MainActivity.this, "Shuffle enabled", Toast.LENGTH_SHORT).show();
            mShuffle.setBackgroundResource(R.drawable.icon_shuffling);
        }
    }
    private void changeBackground(){
        Random random = new Random();
        int newPos = random.nextInt(205);
        switch (newPos){
            case 0:
                mImageView.setImageResource(R.drawable.back_0);
                break;
            case 1 :
                mImageView.setImageResource(R.drawable.back_1);
                break;
            case 2 :
                mImageView.setImageResource(R.drawable.back_2);
                break;
            case 3 :
                mImageView.setImageResource(R.drawable.back_3);
                break;
            case 4 :
                mImageView.setImageResource(R.drawable.back_4);
                break;
            case 5 :
                mImageView.setImageResource(R.drawable.back_5);
                break;
            case 6 :
                mImageView.setImageResource(R.drawable.back_6);
                break;
            case 7 :
                mImageView.setImageResource(R.drawable.back_7);
                break;
            case 8 :
                mImageView.setImageResource(R.drawable.back_8);
                break;
            case 9 :
                mImageView.setImageResource(R.drawable.back_9);
                break;
            case 10 :
                mImageView.setImageResource(R.drawable.back_10);
                break;
            case 11 :
                mImageView.setImageResource(R.drawable.back_11);
                break;
            case 12 :
                mImageView.setImageResource(R.drawable.back_12);
                break;
            case 13 :
                mImageView.setImageResource(R.drawable.back_13);
                break;
            case 14 :
                mImageView.setImageResource(R.drawable.back_14);
                break;
            case 15 :
                mImageView.setImageResource(R.drawable.back_15);
                break;
            case 16 :
                mImageView.setImageResource(R.drawable.back_16);
                break;
            case 17 :
                mImageView.setImageResource(R.drawable.back_17);
                break;
            case 18 :
                mImageView.setImageResource(R.drawable.back_18);
                break;
            case 19 :
                mImageView.setImageResource(R.drawable.back_19);
                break;
            case 20 :
                mImageView.setImageResource(R.drawable.back_20);
                break;
            case 21 :
                mImageView.setImageResource(R.drawable.back_21);
                break;
            case 22 :
                mImageView.setImageResource(R.drawable.back_22);
                break;
            case 23 :
                mImageView.setImageResource(R.drawable.back_23);
                break;
            case 24 :
                mImageView.setImageResource(R.drawable.back_24);
                break;
            case 25 :
                mImageView.setImageResource(R.drawable.back_25);
                break;
            case 26 :
                mImageView.setImageResource(R.drawable.back_26);
                break;
            case 27 :
                mImageView.setImageResource(R.drawable.back_27);
                break;
            case 28 :
                mImageView.setImageResource(R.drawable.back_28);
                break;
            case 29 :
                mImageView.setImageResource(R.drawable.back_29);
                break;
            case 30 :
                mImageView.setImageResource(R.drawable.back_30);
                break;
            case 31 :
                mImageView.setImageResource(R.drawable.back_31);
                break;
            case 32 :
                mImageView.setImageResource(R.drawable.back_32);
                break;
            case 33 :
                mImageView.setImageResource(R.drawable.back_33);
                break;
            case 34 :
                mImageView.setImageResource(R.drawable.back_34);
                break;
            case 35 :
                mImageView.setImageResource(R.drawable.back_35);
                break;
            case 36 :
                mImageView.setImageResource(R.drawable.back_36);
                break;
            case 37 :
                mImageView.setImageResource(R.drawable.back_37);
                break;
            case 38 :
                mImageView.setImageResource(R.drawable.back_38);
                break;
            case 39 :
                mImageView.setImageResource(R.drawable.back_39);
                break;
            case 40 :
                mImageView.setImageResource(R.drawable.back_40);
                break;
            case 41 :
                mImageView.setImageResource(R.drawable.back_41);
                break;
            case 42 :
                mImageView.setImageResource(R.drawable.back_42);
                break;
            case 43 :
                mImageView.setImageResource(R.drawable.back_43);
                break;
            case 44 :
                mImageView.setImageResource(R.drawable.back_44);
                break;
            case 45 :
                mImageView.setImageResource(R.drawable.back_45);
                break;
            case 46 :
                mImageView.setImageResource(R.drawable.back_46);
                break;
            case 47 :
                mImageView.setImageResource(R.drawable.back_47);
                break;
            case 48 :
                mImageView.setImageResource(R.drawable.back_48);
                break;
            case 49 :
                mImageView.setImageResource(R.drawable.back_49);
                break;
            case 50 :
                mImageView.setImageResource(R.drawable.back_50);
                break;
            case 51 :
                mImageView.setImageResource(R.drawable.back_51);
                break;
            case 52 :
                mImageView.setImageResource(R.drawable.back_52);
                break;
            case 53 :
                mImageView.setImageResource(R.drawable.back_53);
                break;
            case 54 :
                mImageView.setImageResource(R.drawable.back_54);
                break;
            case 55 :
                mImageView.setImageResource(R.drawable.back_55);
                break;
            case 56 :
                mImageView.setImageResource(R.drawable.back_56);
                break;
            case 57 :
                mImageView.setImageResource(R.drawable.back_57);
                break;
            case 58 :
                mImageView.setImageResource(R.drawable.back_58);
                break;
            case 59 :
                mImageView.setImageResource(R.drawable.back_59);
                break;
            case 60 :
                mImageView.setImageResource(R.drawable.back_60);
                break;
            case 61 :
                mImageView.setImageResource(R.drawable.back_61);
                break;
            case 62 :
                mImageView.setImageResource(R.drawable.back_62);
                break;
            case 63 :
                mImageView.setImageResource(R.drawable.back_63);
                break;
            case 64 :
                mImageView.setImageResource(R.drawable.back_64);
                break;
            case 65 :
                mImageView.setImageResource(R.drawable.back_65);
                break;
            case 66 :
                mImageView.setImageResource(R.drawable.back_66);
                break;
            case 67 :
                mImageView.setImageResource(R.drawable.back_67);
                break;
            case 68 :
                mImageView.setImageResource(R.drawable.back_68);
                break;
            case 69 :
                mImageView.setImageResource(R.drawable.back_69);
                break;
            case 70 :
                mImageView.setImageResource(R.drawable.back_70);
                break;
            case 71 :
                mImageView.setImageResource(R.drawable.back_71);
                break;
            case 72 :
                mImageView.setImageResource(R.drawable.back_72);
                break;
            case 73 :
                mImageView.setImageResource(R.drawable.back_73);
                break;
            case 74 :
                mImageView.setImageResource(R.drawable.back_74);
                break;
            case 75 :
                mImageView.setImageResource(R.drawable.back_75);
                break;
            case 76 :
                mImageView.setImageResource(R.drawable.back_76);
                break;
            case 77 :
                mImageView.setImageResource(R.drawable.back_77);
                break;
            case 78 :
                mImageView.setImageResource(R.drawable.back_78);
                break;
            case 79 :
                mImageView.setImageResource(R.drawable.back_79);
                break;
            case 80 :
                mImageView.setImageResource(R.drawable.back_80);
                break;
            case 81 :
                mImageView.setImageResource(R.drawable.back_81);
                break;
            case 82 :
                mImageView.setImageResource(R.drawable.back_82);
                break;
            case 83 :
                mImageView.setImageResource(R.drawable.back_83);
                break;
            case 84 :
                mImageView.setImageResource(R.drawable.back_84);
                break;
            case 85 :
                mImageView.setImageResource(R.drawable.back_85);
                break;
            case 86 :
                mImageView.setImageResource(R.drawable.back_86);
                break;
            case 87 :
                mImageView.setImageResource(R.drawable.back_87);
                break;
            case 88 :
                mImageView.setImageResource(R.drawable.back_88);
                break;
            case 89 :
                mImageView.setImageResource(R.drawable.back_89);
                break;
            case 90 :
                mImageView.setImageResource(R.drawable.back_90);
                break;
            case 91 :
                mImageView.setImageResource(R.drawable.back_91);
                break;
            case 92 :
                mImageView.setImageResource(R.drawable.back_92);
                break;
            case 93 :
                mImageView.setImageResource(R.drawable.back_93);
                break;
            case 94 :
                mImageView.setImageResource(R.drawable.back_94);
                break;
            case 95 :
                mImageView.setImageResource(R.drawable.back_95);
                break;
            case 96 :
                mImageView.setImageResource(R.drawable.back_96);
                break;
            case 97 :
                mImageView.setImageResource(R.drawable.back_97);
                break;
            case 98 :
                mImageView.setImageResource(R.drawable.back_98);
                break;
            case 99 :
                mImageView.setImageResource(R.drawable.back_99);
                break;
            case 100 :
                mImageView.setImageResource(R.drawable.back_100);
                break;
            case 101 :
                mImageView.setImageResource(R.drawable.back_101);
                break;
            case 102 :
                mImageView.setImageResource(R.drawable.back_102);
                break;
            case 103 :
                mImageView.setImageResource(R.drawable.back_103);
                break;
            case 104 :
                mImageView.setImageResource(R.drawable.back_104);
                break;
            case 105 :
                mImageView.setImageResource(R.drawable.back_105);
                break;
            case 106 :
                mImageView.setImageResource(R.drawable.back_106);
                break;
            case 107 :
                mImageView.setImageResource(R.drawable.back_107);
                break;
            case 108 :
                mImageView.setImageResource(R.drawable.back_108);
                break;
            case 109 :
                mImageView.setImageResource(R.drawable.back_109);
                break;
            case 110 :
                mImageView.setImageResource(R.drawable.back_110);
                break;
            case 111 :
                mImageView.setImageResource(R.drawable.back_111);
                break;
            case 112 :
                mImageView.setImageResource(R.drawable.back_112);
                break;
            case 113 :
                mImageView.setImageResource(R.drawable.back_113);
                break;
            case 114 :
                mImageView.setImageResource(R.drawable.back_114);
                break;
            case 115 :
                mImageView.setImageResource(R.drawable.back_115);
                break;
            case 116 :
                mImageView.setImageResource(R.drawable.back_116);
                break;
            case 117 :
                mImageView.setImageResource(R.drawable.back_117);
                break;
            case 118 :
                mImageView.setImageResource(R.drawable.back_118);
                break;
            case 119 :
                mImageView.setImageResource(R.drawable.back_119);
                break;
            case 120 :
                mImageView.setImageResource(R.drawable.back_120);
                break;
            case 121 :
                mImageView.setImageResource(R.drawable.back_121);
                break;
            case 122 :
                mImageView.setImageResource(R.drawable.back_122);
                break;
            case 123 :
                mImageView.setImageResource(R.drawable.back_123);
                break;
            case 124 :
                mImageView.setImageResource(R.drawable.back_124);
                break;
            case 125 :
                mImageView.setImageResource(R.drawable.back_125);
                break;
            case 126 :
                mImageView.setImageResource(R.drawable.back_126);
                break;
            case 127 :
                mImageView.setImageResource(R.drawable.back_127);
                break;
            case 128 :
                mImageView.setImageResource(R.drawable.back_128);
                break;
            case 129 :
                mImageView.setImageResource(R.drawable.back_129);
                break;
            case 130 :
                mImageView.setImageResource(R.drawable.back_130);
                break;
            case 131 :
                mImageView.setImageResource(R.drawable.back_131);
                break;
            case 132 :
                mImageView.setImageResource(R.drawable.back_132);
                break;
            case 133 :
                mImageView.setImageResource(R.drawable.back_133);
                break;
            case 134 :
                mImageView.setImageResource(R.drawable.back_134);
                break;
            case 135 :
                mImageView.setImageResource(R.drawable.back_135);
                break;
            case 136 :
                mImageView.setImageResource(R.drawable.back_136);
                break;
            case 137 :
                mImageView.setImageResource(R.drawable.back_137);
                break;
            case 138 :
                mImageView.setImageResource(R.drawable.back_138);
                break;
            case 139 :
                mImageView.setImageResource(R.drawable.back_139);
                break;
            case 140 :
                mImageView.setImageResource(R.drawable.back_140);
                break;
            case 141 :
                mImageView.setImageResource(R.drawable.back_141);
                break;
            case 142 :
                mImageView.setImageResource(R.drawable.back_142);
                break;
            case 143 :
                mImageView.setImageResource(R.drawable.back_143);
                break;
            case 144 :
                mImageView.setImageResource(R.drawable.back_144);
                break;
            case 145 :
                mImageView.setImageResource(R.drawable.back_145);
                break;
            case 146 :
                mImageView.setImageResource(R.drawable.back_146);
                break;
            case 147 :
                mImageView.setImageResource(R.drawable.back_147);
                break;
            case 148 :
                mImageView.setImageResource(R.drawable.back_148);
                break;
            case 149 :
                mImageView.setImageResource(R.drawable.back_149);
                break;
            case 150 :
                mImageView.setImageResource(R.drawable.back_150);
                break;
            case 151 :
                mImageView.setImageResource(R.drawable.back_151);
                break;
            case 152 :
                mImageView.setImageResource(R.drawable.back_152);
                break;
            case 153 :
                mImageView.setImageResource(R.drawable.back_153);
                break;
            case 154 :
                mImageView.setImageResource(R.drawable.back_154);
                break;
            case 155 :
                mImageView.setImageResource(R.drawable.back_155);
                break;
            case 156 :
                mImageView.setImageResource(R.drawable.back_156);
                break;
            case 157 :
                mImageView.setImageResource(R.drawable.back_157);
                break;
            case 158 :
                mImageView.setImageResource(R.drawable.back_158);
                break;
            case 159 :
                mImageView.setImageResource(R.drawable.back_159);
                break;
            case 160 :
                mImageView.setImageResource(R.drawable.back_160);
                break;
            case 161 :
                mImageView.setImageResource(R.drawable.back_161);
                break;
            case 162 :
                mImageView.setImageResource(R.drawable.back_162);
                break;
            case 163 :
                mImageView.setImageResource(R.drawable.back_163);
                break;
            case 164 :
                mImageView.setImageResource(R.drawable.back_164);
                break;
            case 165 :
                mImageView.setImageResource(R.drawable.back_165);
                break;
            case 166 :
                mImageView.setImageResource(R.drawable.back_166);
                break;
            case 167 :
                mImageView.setImageResource(R.drawable.back_167);
                break;
            case 168 :
                mImageView.setImageResource(R.drawable.back_168);
                break;
            case 169 :
                mImageView.setImageResource(R.drawable.back_169);
                break;
            case 170 :
                mImageView.setImageResource(R.drawable.back_170);
                break;
            case 171 :
                mImageView.setImageResource(R.drawable.back_171);
                break;
            case 172 :
                mImageView.setImageResource(R.drawable.back_172);
                break;
            case 173 :
                mImageView.setImageResource(R.drawable.back_173);
                break;
            case 174 :
                mImageView.setImageResource(R.drawable.back_174);
                break;
            case 175 :
                mImageView.setImageResource(R.drawable.back_175);
                break;
            case 176 :
                mImageView.setImageResource(R.drawable.back_176);
                break;
            case 177 :
                mImageView.setImageResource(R.drawable.back_177);
                break;
            case 178 :
                mImageView.setImageResource(R.drawable.back_178);
                break;
            case 179 :
                mImageView.setImageResource(R.drawable.back_179);
                break;
            case 180 :
                mImageView.setImageResource(R.drawable.back_180);
                break;
            case 181 :
                mImageView.setImageResource(R.drawable.back_181);
                break;
            case 182 :
                mImageView.setImageResource(R.drawable.back_182);
                break;
            case 183 :
                mImageView.setImageResource(R.drawable.back_183);
                break;
            case 184 :
                mImageView.setImageResource(R.drawable.back_184);
                break;
            case 185 :
                mImageView.setImageResource(R.drawable.back_185);
                break;
            case 186 :
                mImageView.setImageResource(R.drawable.back_186);
                break;
            case 187 :
                mImageView.setImageResource(R.drawable.back_187);
                break;
            case 188 :
                mImageView.setImageResource(R.drawable.back_188);
                break;
            case 189 :
                mImageView.setImageResource(R.drawable.back_189);
                break;
            case 190 :
                mImageView.setImageResource(R.drawable.back_190);
                break;
            case 191 :
                mImageView.setImageResource(R.drawable.back_191);
                break;
            case 192 :
                mImageView.setImageResource(R.drawable.back_192);
                break;
            case 193 :
                mImageView.setImageResource(R.drawable.back_193);
                break;
            case 194 :
                mImageView.setImageResource(R.drawable.back_194);
                break;
            case 195 :
                mImageView.setImageResource(R.drawable.back_195);
                break;
            case 196 :
                mImageView.setImageResource(R.drawable.back_196);
                break;
            case 197 :
                mImageView.setImageResource(R.drawable.back_197);
                break;
            case 198 :
                mImageView.setImageResource(R.drawable.back_198);
                break;
            case 199 :
                mImageView.setImageResource(R.drawable.back_199);
                break;
            case 200 :
                mImageView.setImageResource(R.drawable.back_200);
                break;
            case 201 :
                mImageView.setImageResource(R.drawable.back_201);
                break;
            case 202 :
                mImageView.setImageResource(R.drawable.back_202);
                break;
            case 203 :
                mImageView.setImageResource(R.drawable.back_203);
                break;
            case 204 :
                mImageView.setImageResource(R.drawable.back_204);
                break;
            default:
                mImageView.setImageResource(R.drawable.back_def);
                break;
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        runtimePermission();
    }
    //TimerBelow
    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRuning = false;
                mBtnStartPause.setText("Start");
                mPause.setBackgroundResource(R.drawable.icon_play);
                myMediaPlayer.pause();
                dialogBuilder();
                mTimer.setText("00:00");
                mBtnStartPause.setEnabled(false);
                mBtnStartPause.setTextColor(Color.GRAY);
                mBtnReset.setEnabled(true);
                mBtnReset.setText("SET");
                mBtnReset.setTextColor(Color.rgb(255, 255, 255));
                mTimerInner.setVisibility(View.VISIBLE);
            }
        }.start();
        mTimerRuning = true;
        mBtnStartPause.setText("STOP");
        mBtnReset.setEnabled(false);
        mBtnReset.setTextColor(Color.GRAY);
        mBtnReset.setText("RESET");
        mTimerOuter.setVisibility(View.GONE);
    }
    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormat = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        mTimer.setText(timeLeftFormat);
    }
    private void resetTimer(long timeMillis) {
        mTimeLeftInMillis = timeMillis;
        updateCountDownText();
        mBtnStartPause.setText("START");
        mBtnStartPause.setTextColor(Color.rgb(255, 255, 255));
        mBtnStartPause.setEnabled(true);
        mBtnReset.setEnabled(false);
        mBtnReset.setTextColor(Color.GRAY);
        mBtnReset.setText("RESET");
        mTimerInner.setVisibility(View.GONE);
    }
    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRuning = false;
        mBtnStartPause.setText("START");
        mBtnReset.setTextColor(Color.rgb(255, 255, 255));
        mBtnReset.setEnabled(true);
    }
    private void dialogBuilder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Time over!");
        builder.setMessage("Track has been paused");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }
    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    //TimerAbove
}
