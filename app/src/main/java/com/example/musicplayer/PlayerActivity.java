package com.example.musicplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

import musicplayer.R;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    Button btnPlay, btnNext, btnPrev, btnFForw, btnRewind;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;
    ImageView imageView;
    String songName;
    int pos;
    ArrayList<File> mySongs;

    Thread updateSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.btnPlay);
        btnRewind = findViewById(R.id.btnRewind);
        btnFForw = findViewById(R.id.btnFastForwd);

        txtSongName = findViewById(R.id.textSong);
        txtSongStart = findViewById(R.id.txtSongStart);
        txtSongEnd = findViewById(R.id.txtSongEnd);

        seekMusicBar = findViewById(R.id.seekBar);
        imageView = findViewById(R.id.imgView);

        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songName");
        pos = bundle.getInt("pos", 0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(pos).toString());
        songName = mySongs.get(pos).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeek = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                //super.run();
            }
        };

        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeek.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.primary_color_v), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.primary_color_v), PorterDuff.Mode.SRC_IN);

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24);
                    mediaPlayer.pause();
                } else {
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                pos = ((pos + 1) % mySongs.size());
                Uri uri = Uri.parse(mySongs.get(pos).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(pos).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                startAnimation(imageView, 360f);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                pos = ((pos - 1) < 0) ? (mySongs.size() - 1) : pos - 1;
                Uri uri = Uri.parse(mySongs.get(pos).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songName = mySongs.get(pos).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                startAnimation(imageView, -360f);
            }
        });

        btnFForw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
            }
        });

        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    if (mediaPlayer.getCurrentPosition() < 10000)
                        mediaPlayer.seekTo(0);
                    else
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });
    }

    public void startAnimation(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);
        objectAnimator.setDuration(500);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time = time + min + ":";
        if (sec < 10)
            time += "0";
        time += sec;
        return time;
    }
}