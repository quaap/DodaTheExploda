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
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class EntryActivity extends Activity {

    private SoundEffects mSoundEffects;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        LinearLayout mv = findViewById(R.id.main_view);

        for (final Mode m: Mode.values()) {
            Log.d("Entry", m.name());

            Button b = new Button(this);
            b.setText(m.toString(this));

            b.setTextSize(36);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(EntryActivity.this, MainActivity.class);
                    i.putExtra("mode", m.name());
                    startActivity(i);
                }
            });

            b.setGravity(Gravity.START);
            mv.addView(b);
        }

        SeekBar volume = findViewById(R.id.volume);

        CheckBox backImage = findViewById(R.id.back_image);

        final SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        volume.setProgress(appPreferences.getInt("sound_effects_volume", 100));

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                appPreferences.edit().putInt("sound_effects_volume", seekBar.getProgress()).apply();
                mSoundEffects.playPlode(0);
            }
        });

        backImage.setChecked(appPreferences.getBoolean("use_back_image", false));

        backImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appPreferences.edit().putBoolean("use_back_image", isChecked).apply();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        mSoundEffects = new SoundEffects(this);

    }

    @Override
    protected void onPause() {
        mSoundEffects.release();
        super.onPause();
    }
}
