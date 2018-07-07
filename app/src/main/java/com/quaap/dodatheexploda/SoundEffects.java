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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


public class SoundEffects implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SoundPool mSounds;

    private Map<Integer, Integer> mPlodeIds = new HashMap<>();
    private Map<Integer, Integer> mMissIds = new HashMap<>();

    private int[] plodeFiles;
    private int[] missFiles;

    private float[] soundVolumes;
    private String[] soundUses;

    private volatile float sfvolume = .9f;
    private volatile float musicvolume = .2f;

    private SharedPreferences appPreferences;

    private volatile boolean mReady = false;

    private volatile boolean mMute = false;

    private Context mContext;



    public SoundEffects(final Context context) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(5)
                    .build();
        } else {
            mSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        appPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());


        appPreferences.registerOnSharedPreferenceChangeListener(this);

        plodeFiles = getResIdArray(context, R.array.plodes);

        int[] vols = context.getResources().getIntArray(R.array.plode_volumes);
        soundVolumes = new float[plodeFiles.length];
        for (int i = 0; i < vols.length; i++) {
            soundVolumes[i] = vols[i] / 100.0f;
        }


        missFiles = getResIdArray(context, R.array.misses);

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < plodeFiles.length; i++) {
                    mPlodeIds.put(i, mSounds.load(context, plodeFiles[i], 1));
                }
                for (int i = 0; i < missFiles.length; i++) {
                    mMissIds.put(i, mSounds.load(context, missFiles[i], 1));
                }
                mReady = true;
            }
        });
    }

    public static int[] getResIdArray(Context context, int id) {
        final TypedArray idsarr = context.getResources().obtainTypedArray(id);
        int[] ids = new int[idsarr.length()];
        for (int i = 0; i < idsarr.length(); i++) {
            ids[i] = idsarr.getResourceId(i, 0);
        }
        idsarr.recycle();
        return ids;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        //Log.d("Pref", s);

        if (s.equals("sound_effects_volume")) {
            sfvolume = sharedPreferences.getInt("sound_effects_volume", 100) / 100.0f;
        }

    }

    private boolean isReady() {
        return mReady;
    }

    public void setMute(boolean mute) {
        mMute = mute;

    }

    public boolean isMuted() {
        return mMute;
    }

    private void loop(int soundKey, int loop) {
        play(soundKey, 1, loop);
    }

    private void play(int soundKey) {
        play(soundKey, 1, 0);
    }

    private void play(int soundKey, float speed, int loop) {
        sfvolume = appPreferences.getInt("sound_effects_volume", 100) / 100.0f;

        try {
            if (isReady() && !mMute && appPreferences.getBoolean("use_sound_effects", true)) {

                float vol = soundVolumes[soundKey] * sfvolume;
                mSounds.play(mPlodeIds.get(soundKey), vol, vol, 1, loop, speed + getRandHundreth());
                Log.d("sfx", soundKey + " key at vol=" + vol);
            }
        } catch (Exception e) {
            Log.e("SoundEffects", "Error playing " + soundKey, e);
        }
    }

    public void playPlode() {
        if (Math.random()<.7) {
            play((int) ((mPlodeIds.size()-2) * Math.random()));
        } else {
            play((int) (mPlodeIds.size() * Math.random()));
        }
    }

    public void playPlode(int which) {
        play(which);
    }

    public void playMiss() {
        sfvolume = appPreferences.getInt("sound_effects_volume", 100) / 100.0f;
        mSounds.play(mMissIds.get((int) (mMissIds.size() * Math.random())), sfvolume, sfvolume, 1, 0, 1 + getRandHundreth()*10);
    }

    private float getRandHundreth() {
        return (float) ((Math.random() - .5) / 100);
    }

    public void release() {
        appPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mSounds.release();
    }
}