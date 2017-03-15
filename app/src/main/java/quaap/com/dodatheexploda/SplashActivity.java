package quaap.com.dodatheexploda;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
                mPlayer = MediaPlayer.create(SplashActivity.this,R.raw.dodadrum);
                mPlayer.setLooping(true);
                mPlayer.start();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.release();
    }
}
