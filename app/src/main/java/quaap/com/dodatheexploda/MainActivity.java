package quaap.com.dodatheexploda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements View.OnClickListener {

    private FrameLayout mMainScreen;
    private LinearLayout mLookFors;


    private Map<TextView,Point> symPoints = new HashMap<>();

    private List<TextView> activeSyms = new ArrayList<>();
    private int current = -1;
    private TextView currentWid = null;
    private TextView currentLookForWid = null;

    private int hints;

    private Mode mMode;
    private Animation notItAnim;
    private Animation wasItAnim;
    private Animation hintAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent!=null) {
            String modestr = intent.getStringExtra("mode");
            if (modestr!=null) {
                mMode = Mode.valueOf(modestr);
            }
        }

        if (mMode==null) {
            mMode = Mode.Baby;
        }

        mMainScreen = (FrameLayout) findViewById(R.id.main_screen);
        mLookFors = (LinearLayout) findViewById(R.id.look_for);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mMode.getBigSize());


        mLookFors.setLayoutParams(lp);


        notItAnim = AnimationUtils.loadAnimation(this, R.anim.not_it);
        wasItAnim = AnimationUtils.loadAnimation(this, R.anim.was_it);
        hintAnim = AnimationUtils.loadAnimation(this, R.anim.hint);


        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 100);

    }


    private void showNext() {
        if (currentLookForWid!=null) {
            currentLookForWid.setBackgroundColor(Color.DKGRAY);
            currentLookForWid.setTextColor(Color.GRAY);
            currentLookForWid.setTextSize(mMode.getMinIconSize()/2);
            currentLookForWid.setOnClickListener(null);
        }
        if (currentWid!=null) {
          //  mMainScreen.removeView(currentWid);
        }

        current++;
        if (current < activeSyms.size()) {
            currentWid = activeSyms.get(current);
            String sym = (String) currentWid.getTag();

            currentLookForWid = new TextView(this);
            currentLookForWid.setText(sym);
            currentLookForWid.setTextSize(mMode.getMinIconSize());
            currentLookForWid.setBackgroundColor(Color.argb(200,127,255,200));

            currentLookForWid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hints++<mMode.getHints()) {
                        currentWid.startAnimation(hintAnim);
                    }
                }
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.setMarginEnd(8);
            lp.setMarginStart(8);

            mLookFors.addView(currentLookForWid, 0, lp);
        } else {
            start();
        }

    }

    @Override
    public void onClick(final View v) {
        String symv = (String)v.getTag();

        TextView wid2 = activeSyms.get(current);
        String symw = (String)wid2.getTag();

        if (symv.equals(symw)) {

            Log.d("Doda", "Found " + symv);
            v.setAlpha(.6f);
            v.startAnimation(wasItAnim);


            final ImageView blow = new ImageView(this);
            blow.setBackgroundResource(R.drawable.explosion);

            Point location = symPoints.get((TextView)v);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(location.x - 65, location.y - 65, 0, 0);
            lp.gravity = Gravity.START | Gravity.TOP;

            blow.setLayoutParams(lp);


            mMainScreen.removeView(v);
            mMainScreen.addView(blow);

            AnimationDrawable ad = ((AnimationDrawable) blow.getBackground());
            int time = ad.getNumberOfFrames() * ad.getDuration(0);
            ad.start();

            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMainScreen.removeView(blow);
                    showNext();
                }
            }, time + 20);

        } else {
            v.startAnimation(notItAnim);

        }

    }

    private void start() {

        current = -1;
        Set<Integer> actives = new HashSet<>();
        mMainScreen.removeAllViews();
        mLookFors.removeAllViews();
        activeSyms.clear();
        symPoints.clear();

        for (int j = 0; j < mMode.getNumIcons(); j++) {

            int symind;
            do {
              symind = getInt(syms.length);
            } while (actives.contains(symind));
            actives.add(symind);

            String sym = syms[symind];

            TextView wid2 = addSymToScreen(sym);
            activeSyms.add(0, wid2);

        }


        showNext();
    }

    private TextView addSymToScreen(String sym) {
        TextView wid2 = new TextView(this);
        wid2.setTag(sym);
        wid2.setText(sym);
        int size = getInt(mMode.getMaxIconSize() - mMode.getMinIconSize())+mMode.getMinIconSize();
        wid2.setTextSize(size);
        wid2.setOnClickListener(this);

        Point location;
        boolean done;
        int tries = 0;
        do {
            done = true;
            location = new Point(getInt(mMainScreen.getWidth()-mMode.getMargin()) + 4, getInt(mMainScreen.getHeight()-mMode.getMargin()) + 4);
            for (Point p: symPoints.values()) {
                if (Math.abs(p.x - location.x) < mMode.getMaxIconSize()/mMode.getOverLap() && Math.abs(p.y - location.y) < mMode.getMaxIconSize()/mMode.getOverLap()) {
                    done = false;
                    break;
                }
            }
        } while (!done && tries++<40);

        symPoints.put(wid2,location);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(location.x - size/2, location.y - size/2, 0 ,0);
        lp.gravity = Gravity.START | Gravity.TOP;
        wid2.setLayoutParams(lp);
        mMainScreen.addView(wid2, lp);
        return wid2;
    }

    private static int getInt(int max) {
        return (int)(Math.random()*max);

    }

    public final static String[] syms;
    private static final int[] symsHex = {
            0x263A, 0x2615, 0x26BE, 0x26F5, 0x26BD, 0x1F680, 0x1F308, 0x1F332, 0x1F333, 0x1F34B,
            0x1F350, 0x1F37C, 0x1F3C7, 0x1F3C9, 0x1F3E4, 0x1F400, 0x1F401, 0x1F402, 0x1F403,
            0x1F404, 0x1F405, 0x1F406, 0x1F407, 0x1F408, 0x1F409, 0x1F40A, 0x1F40B, 0x1F40F,
            0x1F410, 0x1F413, 0x1F415, 0x1F416, 0x1F42A, 0x1F600, 0x1F607, 0x1F608, 0x1F60E,
            0x1F525, 0x1F526, 0x1F527, 0x1F528, 0x1F529, 0x1F52E, 0x1F530, 0x1F531, 0x1F4DA,
            0x1F498, 0x1F482, 0x1F483, 0x1F484, 0x1F485, 0x1F4F7, 0x1F4F9, 0x1F4FA, 0x1F4FB,
            0x1F30D, 0x1F30E, 0x1F310, 0x1F312, 0x1F316, 0x1F317, 0x1F318, 0x1F31A, 0x1F31C,
            0x1F31D, 0x1F31E, 0x1F681, 0x1F682, 0x1F686, 0x1F688, 0x1F334, 0x1F335, 0x1F337,
            0x1F338, 0x1F339, 0x1F33A, 0x1F33B, 0x1F33C, 0x1F33D, 0x1F33E, 0x1F33F, 0x1F340,
            0x1F341, 0x1F342, 0x1F343, 0x1F344, 0x1F345, 0x1F346, 0x1F347, 0x1F348, 0x1F349,
            0x1F34A, 0x1F34C, 0x1F34D, 0x1F34E, 0x1F34F, 0x1F351, 0x1F352, 0x1F353, 0x1F354,
            0x1F355, 0x1F356, 0x1F357, 0x1F35A, 0x1F35B, 0x1F35C, 0x1F35D, 0x1F35E, 0x1F35F,
            0x1F360, 0x1F361, 0x1F362, 0x1F363, 0x1F364, 0x1F365, 0x1F366, 0x1F367, 0x1F368,
            0x1F369, 0x1F36A, 0x1F36B, 0x1F36C, 0x1F36D, 0x1F36E, 0x1F36F, 0x1F370, 0x1F371,
            0x1F372, 0x1F373, 0x1F374, 0x1F375, 0x1F376, 0x1F377, 0x1F378, 0x1F379, 0x1F37A,
            0x1F37B, 0x1F380, 0x1F381, 0x1F382, 0x1F383, 0x1F384, 0x1F385, 0x1F386, 0x1F387,
            0x1F388, 0x1F389, 0x1F38A, 0x1F38B, 0x1F38C, 0x1F38D, 0x1F38E, 0x1F38F, 0x1F390,
            0x1F391, 0x1F392, 0x1F393
    };

    static {
        syms = new String[symsHex.length];
        for (int i = 0; i < symsHex.length; i++) {
            syms[i] = new String(Character.toChars(symsHex[i]));
        }
    }


}
