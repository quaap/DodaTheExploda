package quaap.com.dodatheexploda;

/**
 * Created by tom on 3/13/17.
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

    private Map<Integer, Integer> mSoundIds = new HashMap<>();

    private int[] soundFiles;

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

        soundFiles = getResIdArray(context, R.array.plodes);

        int[] vols = context.getResources().getIntArray(R.array.plode_volumes);
        soundVolumes = new float[soundFiles.length];
        for (int i = 0; i < vols.length; i++) {
            soundVolumes[i] = vols[i] / 100.0f;
        }



        new Handler().post(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < soundFiles.length; i++) {
                    mSoundIds.put(i, mSounds.load(context, soundFiles[i], 1));
                }
                mReady = true;
            }
        });
    }

    private static int[] getResIdArray(Context context, int id) {
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

                float vol = soundVolumes[soundKey] * sfvolume + getRandHundreth();
                mSounds.play(mSoundIds.get(soundKey), vol, vol, 1, loop, speed + getRandHundreth());
                Log.d("sfx", soundKey + " key");
            }
        } catch (Exception e) {
            Log.e("SoundEffects", "Error playing " + soundKey, e);
        }
    }

    public void playPlode() {
        if (Math.random()<.7) {
            play((int) ((mSoundIds.size()-2) * Math.random()));
        } else {
            play((int) (mSoundIds.size() * Math.random()));
        }
    }


    private float getRandHundreth() {
        return (float) ((Math.random() - .5) / 100);
    }

    public void release() {
        appPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mSounds.release();
    }
}