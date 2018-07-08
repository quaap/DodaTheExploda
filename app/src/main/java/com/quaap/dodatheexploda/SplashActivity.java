package com.quaap.dodatheexploda;

/**
 * Copyright (C) 2017   Tom Kliethermes
 *
 * This file is part of DodaTheExploda and is is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;

public class SplashActivity extends Activity implements View.OnClickListener{

    private MediaPlayer mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.go_textView).setOnClickListener(this);
        findViewById(R.id.go_button).setOnClickListener(this);
        findViewById(R.id.go_imageView).setOnClickListener(this);

        findViewById(R.id.about_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mPlayer = MediaPlayer.create(SplashActivity.this,R.raw.dodadrum);
                mPlayer.setLooping(true);
                float vol = appPreferences.getInt("sound_effects_volume", 100) / 100.0f;;
                mPlayer.setVolume(vol,vol);
                mPlayer.start();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPlayer!=null) {
            mPlayer.release();
        }
    }
}
