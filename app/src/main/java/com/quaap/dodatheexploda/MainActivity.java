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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements DodaView.OnItemTouchListener {

    public static final int START_DURATION = 2000;
    private DodaView mMainScreen;

    private LinearLayout mGameOverScreen;

    private LinearLayout mLevelCompleteScreen;

    private String currentWid = null;
    private TextView currentLookForWid = null;
    private TextView score1 = null;
    private TextView score2 = null;
    private TextView score3 = null;

    private int hints;

    private Mode mMode;

    private int bsize;

    //private long startTime;
    private int timeAllowed;
    private int ticksTaken;

    private long findTime;
    private long score;

    Timer timer;

    private SoundEffects mSoundEffects;

    private boolean backgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar ab = getActionBar();
        if (ab!=null) {
            ab.hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

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

        timeAllowed = mMode.getTimeAllowed();


        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        backgroundImage = appPreferences.getBoolean("use_back_image", false);

        mMainScreen = findViewById(R.id.main_screen);
        mLevelCompleteScreen = findViewById(R.id.level_complete_screen);
        mGameOverScreen = findViewById(R.id.game_over_screen);

        currentLookForWid = findViewById(R.id.looking_for);
        score1 = findViewById(R.id.score1);
        score2 = findViewById(R.id.score2);
        score3 = findViewById(R.id.score3);



        bsize = getSmallestDim();


        Log.d("Doda", mMode.getIconSize(bsize) + " " + bsize);

        currentLookForWid.setTextSize(Math.max(mMode.getMinIconSize(bsize), 40));


        mMainScreen.setOnItemTouchListener(this);


        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 100);


        View.OnClickListener hintClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentWid!=null && (!mMode.limitHints() || hints<mMode.getHints())) {
                    hints++;
                    mMainScreen.highlightTop();
                    updateScoreBoard();
                }
            }
        };

        currentLookForWid.setOnClickListener(hintClick);
        score3.setOnClickListener(hintClick);

        findViewById(R.id.menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMenu();
            }
        });

        findViewById(R.id.menu_button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMenu();
            }
        });

        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
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
        if (timer!=null) {
            timer.cancel();
        }
        mSoundEffects.release();
        super.onPause();
    }

    private void returnToMenu() {
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
        finish();
    }


    private void endGame() {

        mMainScreen.removeAllItems();


        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {

                mGameOverScreen.setVisibility(View.VISIBLE);
                mMainScreen.setVisibility(View.GONE);

            }
        },500);


    }

    private void levelComplete() {
        if (timer!=null) {
            timer.cancel();
        }
        if (timeAllowed>5) timeAllowed *= .92;

        score2.setText(getString(R.string.score_time,  timeAllowed));

        updateScoreBoard();

        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentLookForWid.setText("");
                TextView faster = mLevelCompleteScreen.findViewById(R.id.faster);

                faster.setVisibility(mMode.isTimed()? View.VISIBLE : View.GONE);

                mLevelCompleteScreen.setVisibility(View.VISIBLE);
                mMainScreen.setVisibility(View.GONE);

            }
        },200);


    }


    private void updateScoreBoard() {
        score1.setText(getString(R.string.score_found, mMainScreen.count(), mMode.getNumIcons(), score));
        if (mMode.limitHints()) {
            score3.setText(getString(R.string.score_hints, mMode.getHints() - hints));
        }
    }

    private void showNext(boolean wiggle) {
        currentWid = mMainScreen.peek(!mMode.limitHints() || hints<mMode.getHints());
        if (currentWid != null) {

            currentLookForWid.setText(currentWid);

            if (wiggle) {
                scheduleHint(2000);
                scheduleHint(5000);
            }
            scheduleHint(30000);

            findTime = System.currentTimeMillis();

        } else {
            if (timer!=null) {
                timer.cancel();
            }

            int delaytime = 1000;
            String message = "";

            if (mMode.limitHints()) {
                int bonus =(mMode.getHints() - hints)*1000;
                score += bonus;

                message += getString(R.string.hint_bonus, bonus);
                delaytime = 2500;
            }

            if (mMode.isTimed() && ticksTaken<timeAllowed) {
                long bonus = (timeAllowed - ticksTaken)*1000;
                score += bonus;
                delaytime = 2500;

                if (!message.equals("")) message += "\n";
                message += getString(R.string.time_bonus,  bonus);
            }

            showMessage(message);

            mMainScreen.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mMode.showLevelComplete()) {
                        levelComplete();
                    } else {
                        start();
                    }
                }
            },delaytime);
        }

        updateScoreBoard();
    }



    private void scheduleHint(int time) {
        final String currentWidThen = currentWid;
        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentWidThen.equals(currentWid)) {
                    mMainScreen.highlightTop();
                }
            }
        }, time);
    }

    @Override
    public void onItemClick(String text) {
        //Log.d("Doda", "touched '" + text +"'");

        if (currentLookForWid.getText().equals(" ")) {
            return;
        }

        if (currentWid==null || text==null) return;

        if (currentWid.equals(text)) {

            Log.d("Doda", "Found " + currentWid.codePointAt(0));

            score += Math.max(100, 5000 - (System.currentTimeMillis() - findTime)) * (backgroundImage?1.5:1);

            mSoundEffects.playPlode();
            mMainScreen.startPlode();

            mMainScreen.pop();

            showNext(false);

        } else {
            mSoundEffects.playMiss();
            mMainScreen.highlight(text);

            switch (mMode) {
                case Adult:
                case AdultTimed:
                    score -= 100;
                    break;
                case Child:
                case ChildTimed:
                    score -= 25;
                    break;
            }
            if (mMode.isTimed() && mMode==Mode.AdultTimed) {
                ticksTaken += 5;
                showMessage(getString(R.string.miss_penalty));
            }
        }
        updateScoreBoard();

    }

    private void showMessage(String message) {
        final TextView t = findViewById(R.id.message_text);
        t.setTextSize(36);
        t.setShadowLayer(16, 2, 2, Color.WHITE);
        t.setText(message);
        t.setTextColor(Color.BLACK);
        t.setBackgroundColor(Color.argb(127,64,64,64));
        t.setVisibility(View.VISIBLE);

        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                t.setVisibility(View.GONE);
            }
        }, 2000);

    }

    private void start() {

        currentWid = null;
        mMainScreen.removeAllItems();
        mMainScreen.setVisibility(View.VISIBLE);
        mLevelCompleteScreen.setVisibility(View.GONE);
        mGameOverScreen.setVisibility(View.GONE);

        currentLookForWid.setText(" ");

        if (backgroundImage) {
            int[] backs = SoundEffects.getResIdArray(this, R.array.pics);

            mMainScreen.setBackgroundResource(backs[(int) (backs.length * Math.random())]);
        }

        hints = 0;
        ticksTaken = 0;
        if (timer!=null) {
            timer.cancel();
        }


        long ttime = START_DURATION / mMode.getNumIcons();
        Set<Integer> actives = new HashSet<>();
        int bail = syms.length * 2;
        for (int j = 0; j < mMode.getNumIcons(); j++) {

            int symind;
            String sym;
            int tries = 0;
            do {
                int triesInner = 0;
                do {
                    symind = getRandomInt(syms.length);
                } while (actives.contains(symind) && triesInner++<bail);

                sym = syms[symind];
            } while (!hasGlyph(sym) && tries++<bail);
            actives.add(symind);

            addSymToScreen(sym, ttime*j, j == mMode.getNumIcons()-1);

        }


    }

    private void startDone() {
        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNext(true);
                updateScoreBoard();
            }
        }, 200);

        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                ticksTaken = 0;

                updateScoreBoard();

                if (mMode.isTimed()) {
                    score2.setVisibility(View.VISIBLE);
                    if (timer!=null) {
                        timer.cancel();
                    }
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            score2.post(new Runnable() {
                                @Override
                                public void run() {
                                    ticksTaken++;
                                    int timeleft = timeAllowed - ticksTaken;
                                    score2.setText(getString(R.string.score_time,  timeleft));
                                    if (timeleft <= 0) {
                                        timer.cancel();
                                        endGame();
                                    }
                                }
                            });
                        }
                    }, 1000, 1000);
                } else {
                    score2.setVisibility(View.GONE);
                }

            }
        }, 350);
    }


    private void addSymToScreen(final String sym, long delay, final boolean isLast) {
        Point location;
        boolean done;
        final int size = (getRandomInt(mMode.getMaxIconSize(bsize) - mMode.getMinIconSize(bsize))+mMode.getMinIconSize(bsize))*2;

        mMainScreen.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMainScreen.addText(size, sym);
                if (isLast) {
                    startDone();
                }
            }
        }, delay);

    }

    private static int getRandomInt(int max) {
        return (int)(Math.random()*max);

    }

    private int getSmallestDim() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return Math.min(size.x, size.y);
    }

    private int spToPx(int px) {
        return (int)(px * getResources().getDisplayMetrics().density + 0.5f);
    }


    private static final Paint hasGlyphTester = new Paint();
    private static final String basicSmiley = new String(Character.toChars(0x1F600));
    private static boolean hasGlyph(String sym) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                return hasGlyphTester.hasGlyph(sym);
            } else {

                float testW = hasGlyphTester.measureText(sym);
                float knownW = hasGlyphTester.measureText(basicSmiley);
                return testW > knownW * .5;
            }
        } catch (Exception e) {
            Log.e("Doda", e.getMessage(), e);
        }

        return false;
    }


    public final static String[] syms;
    private static final int[] symsHex = {
            0x1F601, //grinning face with smiling eyes
            0x1F60D, //smiling face with heart-shaped eyes
            0x1F60F, //smirking face
            0x1F612, //unamused face
            0x1F629, //weary face
            0x1F62A, //sleepy face
            0x1F62B, //tired face
            0x1F62D, //loudly crying face
            0x1F630, //face with open mouth and cold sweat
            0x1F633, //flushed face
            0x1F635, //dizzy face
            0x1F637, //face with medical mask
            0x1F638, //grinning cat face with smiling eyes
            0x1F639, //cat face with tears of joy
            0x1F63A, //smiling cat face with open mouth
            0x1F63B, //smiling cat face with heart-shaped eyes
            0x1F63C, //cat face with wry smile
            0x1F63D, //kissing cat face with closed eyes
            0x1F63E, //pouting cat face
            0x1F63F, //crying cat face
            0x1F640, //weary cat face
            0x1F645, //face with no good gesture
            0x1F646, //face with ok gesture
            0x1F647, //person bowing deeply
            0x1F648, //see-no-evil monkey
            0x1F649, //hear-no-evil monkey
            0x1F64A, //speak-no-evil monkey
            0x1F64B, //happy person raising one hand
            0x1F64C, //person raising both hands in celebration
            0x1F64D, //person frowning
            0x1F64E, //person with pouting face
            0x1F64F, //person with folded hands
            0x2702, //black scissors
            0x2705, //white heavy check mark
            0x2708, //airplane
            0x2709, //envelope
            0x270A, //raised fist
            0x270B, //raised hand
            0x270C, //victory hand
            0x270F, //pencil
            0x2712, //black nib
            0x2714, //heavy check mark
            0x2716, //heavy multiplication x
            0x2728, //sparkles
            0x2733, //eight spoked asterisk
            0x2734, //eight pointed black star
            0x2744, //snowflake
            0x2747, //sparkle
            0x274C, //cross mark
            0x274E, //negative squared cross mark
            0x2753, //black question mark ornament
            0x2754, //white question mark ornament
            0x2755, //white exclamation mark ornament
            0x2757, //heavy exclamation mark symbol
            0x2764, //heavy black heart
            0x2795, //heavy plus sign
            0x2796, //heavy minus sign
            0x2797, //heavy division sign
            0x27A1, //black rightwards arrow
            0x27B0, //curly loop
            0x1F680, //rocket
            0x1F683, //railway car
            0x1F684, //high-speed train
            0x1F685, //high-speed train with bullet nose
            0x1F687, //metro
            0x1F689, //station
            0x1F68C, //bus
            0x1F68F, //bus stop
            0x1F691, //ambulance
            0x1F692, //fire engine
            0x1F693, //police car
            0x1F695, //taxi
            0x1F697, //automobile
            0x1F699, //recreational vehicle
            0x1F69A, //delivery truck
            0x1F6A2, //ship
            0x1F6A4, //speedboat
            0x1F6A5, //horizontal traffic light
            0x1F6A7, //construction sign
            0x1F6A8, //police cars revolving light
            0x1F6A9, //triangular flag on post
            0x1F6AA, //door
            0x1F6AB, //no entry sign
            0x1F6B2, //bicycle
            0x1F6B6, //pedestrian
            0x1F6B9, //mens symbol
            0x1F6BA, //womens symbol
            0x1F6BB, //restroom
            0x1F6BC, //baby symbol
            0x1F6BD, //toilet
            0x1F6BE, //water closet
            0x1F6C0, //bath
            0x2600, //black sun with rays
            0x2601, //cloud
            0x260E, //black telephone
            0x2611, //ballot box with check
            0x2614, //umbrella with rain drops
            0x2615, //hot beverage
            0x261D, //white up pointing index
            0x263A, //white smiling face
            0x2648, //aries
            0x2649, //taurus
            0x264A, //gemini
            0x264B, //cancer
            0x264C, //leo
            0x264D, //virgo
            0x264E, //libra
            0x264F, //scorpius
            0x2650, //sagittarius
            0x2651, //capricorn
            0x2652, //aquarius
            0x2653, //pisces
            0x2660, //black spade suit
            0x2663, //black club suit
            0x2665, //black heart suit
            0x2666, //black diamond suit
            0x2668, //hot springs
            0x267B, //black universal recycling symbol
            0x267F, //wheelchair symbol
            0x2693, //anchor
            0x26A0, //warning sign
            0x26A1, //high voltage sign
            0x26BD, //soccer ball
            0x26BE, //baseball
            0x26C4, //snowman without snow
            0x26C5, //sun behind cloud
            0x26CE, //ophiuchus
            0x26D4, //no entry
            0x26EA, //church
            0x26F2, //fountain
            0x26F3, //flag in hole
            0x26F5, //sailboat
            0x26FA, //tent
            0x26FD, //fuel pump
            0x1F004, //mahjong tile red dragon
            0x1F0CF, //playing card black joker
            0x1F300, //cyclone
            0x1F301, //foggy
            0x1F302, //closed umbrella
            0x1F303, //night with stars
            0x1F304, //sunrise over mountains
            0x1F305, //sunrise
            0x1F306, //cityscape at dusk
            0x1F307, //sunset over buildings
            0x1F308, //rainbow
            0x1F309, //bridge at night
            0x1F30A, //water wave
            0x1F30B, //volcano
            0x1F30F, //earth globe asia-australia
            0x1F311, //new moon symbol
            0x1F313, //first quarter moon symbol
            0x1F314, //waxing gibbous moon symbol
            0x1F315, //full moon symbol
            0x1F319, //crescent moon
            0x1F31B, //first quarter moon with face
            0x1F31F, //glowing star
            0x1F320, //shooting star
            0x1F330, //chestnut
            0x1F331, //seedling
            0x1F334, //palm tree
            0x1F335, //cactus
            0x1F337, //tulip
            0x1F338, //cherry blossom
            0x1F339, //rose
            0x1F33A, //hibiscus
            0x1F33B, //sunflower
            0x1F33C, //blossom
            0x1F33D, //ear of maize
            0x1F33E, //ear of rice
            0x1F33F, //herb
            0x1F340, //four leaf clover
            0x1F341, //maple leaf
            0x1F342, //fallen leaf
            0x1F343, //leaf fluttering in wind
            0x1F344, //mushroom
            0x1F345, //tomato
            0x1F346, //aubergine
            0x1F347, //grapes
            0x1F348, //melon
            0x1F349, //watermelon
            0x1F34A, //tangerine
            0x1F34C, //banana
            0x1F34D, //pineapple
            0x1F34E, //red apple
            0x1F34F, //green apple
            0x1F351, //peach
            0x1F352, //cherries
            0x1F353, //strawberry
            0x1F354, //hamburger
            0x1F355, //slice of pizza
            0x1F356, //meat on bone
            0x1F357, //poultry leg
            0x1F35A, //cooked rice
            0x1F35B, //curry and rice
            0x1F35C, //steaming bowl
            0x1F35D, //spaghetti
            0x1F35E, //bread
            0x1F35F, //french fries
            0x1F360, //roasted sweet potato
            0x1F361, //dango
            0x1F362, //oden
            0x1F363, //sushi
            0x1F364, //fried shrimp
            0x1F365, //fish cake with swirl design
            0x1F366, //soft ice cream
            0x1F367, //shaved ice
            0x1F368, //ice cream
            0x1F369, //doughnut
            0x1F36A, //cookie
            0x1F36B, //chocolate bar
            0x1F36C, //candy
            0x1F36D, //lollipop
            0x1F36E, //custard
            0x1F36F, //honey pot
            0x1F370, //shortcake
            0x1F371, //bento box
            0x1F372, //pot of food
            0x1F373, //cooking
            0x1F374, //fork and knife
            0x1F375, //teacup without handle
            0x1F380, //ribbon
            0x1F381, //wrapped present
            0x1F382, //birthday cake
            0x1F383, //jack-o-lantern
            0x1F384, //christmas tree
            0x1F385, //father christmas
            0x1F386, //fireworks
            0x1F387, //firework sparkler
            0x1F388, //balloon
            0x1F389, //party popper
            0x1F38A, //confetti ball
            0x1F38B, //tanabata tree
            0x1F38C, //crossed flags
            0x1F38D, //pine decoration
            0x1F38E, //japanese dolls
            0x1F38F, //carp streamer
            0x1F390, //wind chime
            0x1F391, //moon viewing ceremony
            0x1F392, //school satchel
            0x1F393, //graduation cap
            0x1F3A0, //carousel horse
            0x1F3A1, //ferris wheel
            0x1F3A2, //roller coaster
            0x1F3A3, //fishing pole and fish
            0x1F3A4, //microphone
            0x1F3A5, //movie camera
            0x1F3A6, //cinema
            0x1F3A7, //headphone
            0x1F3A8, //artist palette
            0x1F3A9, //top hat
            0x1F3AA, //circus tent
            0x1F3AB, //ticket
            0x1F3AC, //clapper board
            0x1F3AD, //performing arts
            0x1F3AE, //video game
            0x1F3AF, //direct hit
            0x1F3B0, //slot machine
            0x1F3B1, //billiards
            0x1F3B2, //game die
            0x1F3B3, //bowling
            0x1F3B4, //flower playing cards
            0x1F3B5, //musical note
            0x1F3B6, //multiple musical notes
            0x1F3B7, //saxophone
            0x1F3B8, //guitar
            0x1F3B9, //musical keyboard
            0x1F3BA, //trumpet
            0x1F3BB, //violin
            0x1F3BC, //musical score
            0x1F3BD, //running shirt with sash
            0x1F3BE, //tennis racquet and ball
            0x1F3BF, //ski and ski boot
            0x1F3C0, //basketball and hoop
            0x1F3C1, //chequered flag
            0x1F3C2, //snowboarder
            0x1F3C3, //runner
            0x1F3C4, //surfer
            0x1F3C6, //trophy
            0x1F3C8, //american football
            0x1F3CA, //swimmer
            0x1F3E0, //house building
            0x1F3E1, //house with garden
            0x1F3E2, //office building
            0x1F3E3, //japanese post office
            0x1F3E5, //hospital
            0x1F3E6, //bank
            0x1F3E7, //automated teller machine
            0x1F3E8, //hotel
            0x1F3E9, //love hotel
            0x1F3EA, //convenience store
            0x1F3EB, //school
            0x1F3EC, //department store
            0x1F3ED, //factory
            0x1F3EE, //izakaya lantern
            0x1F3EF, //japanese castle
            0x1F3F0, //european castle
            0x1F40C, //snail
            0x1F40D, //snake
            0x1F40E, //horse
            0x1F411, //sheep
            0x1F412, //monkey
            0x1F414, //chicken
            0x1F417, //boar
            0x1F418, //elephant
            0x1F419, //octopus
            0x1F41A, //spiral shell
            0x1F41B, //bug
            0x1F41C, //ant
            0x1F41D, //honeybee
            0x1F41E, //lady beetle
            0x1F41F, //fish
            0x1F420, //tropical fish
            0x1F421, //blowfish
            0x1F422, //turtle
            0x1F423, //hatching chick
            0x1F424, //baby chick
            0x1F425, //front-facing baby chick
            0x1F426, //bird
            0x1F427, //penguin
            0x1F428, //koala
            0x1F429, //poodle
            0x1F42B, //bactrian camel
            0x1F42C, //dolphin
            0x1F42D, //mouse face
            0x1F42E, //cow face
            0x1F42F, //tiger face
            0x1F430, //rabbit face
            0x1F431, //cat face
            0x1F432, //dragon face
            0x1F433, //spouting whale
            0x1F434, //horse face
            0x1F435, //monkey face
            0x1F436, //dog face
            0x1F437, //pig face
            0x1F438, //frog face
            0x1F439, //hamster face
            0x1F43A, //wolf face
            0x1F43B, //bear face
            0x1F43C, //panda face
            0x1F43D, //pig nose
            0x1F43E, //paw prints
            0x1F440, //eyes
            0x1F442, //ear
            0x1F443, //nose
            0x1F444, //mouth
            0x1F445, //tongue
            0x1F446, //white up pointing backhand index
            0x1F447, //white down pointing backhand index
            0x1F448, //white left pointing backhand index
            0x1F449, //white right pointing backhand index
            0x1F44A, //fisted hand sign
            0x1F44B, //waving hand sign
            0x1F44C, //ok hand sign
            0x1F44D, //thumbs up sign
            0x1F44E, //thumbs down sign
            0x1F44F, //clapping hands sign
            0x1F450, //open hands sign
            0x1F451, //crown
            0x1F452, //womans hat
            0x1F453, //eyeglasses
            0x1F454, //necktie
            0x1F455, //t-shirt
            0x1F456, //jeans
            0x1F457, //dress
            0x1F458, //kimono
            0x1F459, //bikini
            0x1F45A, //womans clothes
            0x1F45B, //purse
            0x1F45C, //handbag
            0x1F45D, //pouch
            0x1F45E, //mans shoe
            0x1F45F, //athletic shoe
            0x1F460, //high-heeled shoe
            0x1F461, //womans sandal
            0x1F462, //womans boots
            0x1F463, //footprints
            0x1F464, //bust in silhouette
            0x1F466, //boy
            0x1F467, //girl
            0x1F468, //man
            0x1F469, //woman
            0x1F46A, //family
            0x1F46B, //man and woman holding hands
            0x1F46E, //police officer
            0x1F46F, //woman with bunny ears
            0x1F470, //bride with veil
            0x1F471, //person with blond hair
            0x1F472, //man with gua pi mao
            0x1F473, //man with turban
            0x1F474, //older man
            0x1F475, //older woman
            0x1F476, //baby
            0x1F477, //construction worker
            0x1F478, //princess
            0x1F479, //japanese ogre
            0x1F47A, //japanese goblin
            0x1F47B, //ghost
            0x1F47C, //baby angel
            0x1F47D, //extraterrestrial alien
            0x1F47E, //alien monster
            0x1F47F, //imp
            0x1F480, //skull
            0x1F481, //information desk person
            0x1F482, //guardsman
            0x1F483, //dancer
            0x1F484, //lipstick
            0x1F485, //nail polish
            0x1F486, //face massage
            0x1F487, //haircut
            0x1F488, //barber pole
            0x1F48B, //kiss mark
            0x1F48C, //love letter
            0x1F48D, //ring
            0x1F48E, //gem stone
            0x1F48F, //kiss
            0x1F490, //bouquet
            0x1F491, //couple with heart
            0x1F492, //wedding
            0x1F493, //beating heart
            0x1F494, //broken heart
            0x1F495, //two hearts
            0x1F496, //sparkling heart
            0x1F497, //growing heart
            0x1F498, //heart with arrow
            0x1F499, //blue heart
            0x1F49A, //green heart
            0x1F49B, //yellow heart
            0x1F49C, //purple heart
            0x1F49D, //heart with ribbon
            0x1F49E, //revolving hearts
            0x1F49F, //heart decoration
            0x1F4A0, //diamond shape with a dot inside
            0x1F4A1, //electric light bulb
            0x1F4A2, //anger symbol
            0x1F4A3, //bomb
            0x1F4A4, //sleeping symbol
            0x1F4A5, //collision symbol
            0x1F4A6, //splashing sweat symbol
            0x1F4A7, //droplet
            0x1F4A8, //dash symbol
            0x1F4A9, //pile of poo
            0x1F4AA, //flexed biceps
            0x1F4AB, //dizzy symbol
            0x1F4AC, //speech balloon
            0x1F4AE, //white flower
            0x1F4AF, //hundred points symbol
            0x1F4B0, //money bag
            0x1F4B1, //currency exchange
            0x1F4B2, //heavy dollar sign
            0x1F4B3, //credit card
            0x1F4B4, //banknote with yen sign
            0x1F4B5, //banknote with dollar sign
            0x1F4B8, //money with wings
            0x1F4B9, //chart with upwards trend and yen sign
            0x1F4BA, //seat
            0x1F4BB, //personal computer
            0x1F4BC, //briefcase
            0x1F4BD, //minidisc
            0x1F4BE, //floppy disk
            0x1F4BF, //optical disc
            0x1F4C0, //dvd
            0x1F4C1, //file folder
            0x1F4C2, //open file folder
            0x1F4C3, //page with curl
            0x1F4C4, //page facing up
            0x1F4C5, //calendar
            0x1F4C6, //tear-off calendar
            0x1F4C7, //card index
            0x1F4C8, //chart with upwards trend
            0x1F4C9, //chart with downwards trend
            0x1F4CA, //bar chart
            0x1F4CB, //clipboard
            0x1F4CC, //pushpin
            0x1F4CD, //round pushpin
            0x1F4CE, //paperclip
            0x1F4CF, //straight ruler
            0x1F4D0, //triangular ruler
            0x1F4D1, //bookmark tabs
            0x1F4D2, //ledger
            0x1F4D3, //notebook
            0x1F4D4, //notebook with decorative cover
            0x1F4D5, //closed book
            0x1F4D6, //open book
            0x1F4D7, //green book
            0x1F4D8, //blue book
            0x1F4D9, //orange book
            0x1F4DA, //books
            0x1F4DB, //name badge
            0x1F4DC, //scroll
            0x1F4DD, //memo
            0x1F4DE, //telephone receiver
            0x1F4DF, //pager
            0x1F4E0, //fax machine
            0x1F4E1, //satellite antenna
            0x1F4E2, //public address loudspeaker
            0x1F4E3, //cheering megaphone
            0x1F4E4, //outbox tray
            0x1F4E5, //inbox tray
            0x1F4E6, //package
            0x1F4E7, //e-mail symbol
            0x1F4E8, //incoming envelope
            0x1F4E9, //envelope with downwards arrow above
            0x1F4EA, //closed mailbox with lowered flag
            0x1F4EB, //closed mailbox with raised flag
            0x1F4EE, //postbox
            0x1F4F0, //newspaper
            0x1F4F7, //camera
            0x1F4F9, //video camera
            0x1F4FA, //television
            0x1F4FB, //radio
            0x1F4FC, //videocassette
            0x1F525, //fire
            0x1F526, //electric torch
            0x1F527, //wrench
            0x1F528, //hammer
            0x1F529, //nut and bolt
            0x1F52A, //hocho
            0x1F52E, //crystal ball
            0x1F534, //large red circle
            0x1F535, //large blue circle
            0x1F536, //large orange diamond
            0x1F537, //large blue diamond
            0x1F5FB, //mount fuji
            0x1F5FC, //tokyo tower
            0x1F5FD, //statue of liberty
            0x1F681, //helicopter
            0x1F682, //steam locomotive
            0x1F686, //train
            0x1F688, //light rail
            0x1F68A, //tram
            0x1F68D, //oncoming bus
            0x1F68E, //trolleybus
            0x1F690, //minibus
            0x1F694, //oncoming police car
            0x1F696, //oncoming taxi
            0x1F698, //oncoming automobile
            0x1F69B, //articulated lorry
            0x1F69C, //tractor
            0x1F69D, //monorail
            0x1F69E, //mountain railway
            0x1F69F, //suspension railway
            0x1F6A0, //mountain cableway
            0x1F6A1, //aerial tramway
            0x1F6A3, //rowboat
            0x1F6A6, //vertical traffic light
            0x1F6AE, //put litter in its place symbol
            0x1F6AF, //do not litter symbol
            0x1F6B0, //potable water symbol
            0x1F6B1, //non-potable water symbol
            0x1F6B3, //no bicycles
            0x1F6B4, //bicyclist
            0x1F6B5, //mountain bicyclist
            0x1F6B7, //no pedestrians
            0x1F6B8, //children crossing
            0x1F6BF, //shower
            0x1F6C1, //bathtub
            0x1F6C2, //passport control
            0x1F6C3, //customs
            0x1F6C4, //baggage claim
            0x1F6C5, //left luggage

    };

    static {
        syms = new String[symsHex.length];
        for (int i = 0; i < symsHex.length; i++) {
            syms[i] = new String(Character.toChars(symsHex[i]));
            if (!hasGlyph(syms[i])) {
                Log.d("Doda", "No glyph for " + i + " " + symsHex[i]);
            }

        }
    }


}
