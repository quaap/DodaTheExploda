package com.quaap.dodatheexploda;

import android.content.Context;

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


public enum Mode {

    Baby   (3, -1, .5f, -1, R.string.level_baby, R.integer.level_baby_icon),
    Toddler(7, -1, 1, -1, R.string.level_toddler, R.integer.level_toddler_icon),
    Child  (25, 0, 2, 15, R.string.level_child, R.integer.level_child_icon),
    ChildTimed  (25, 120, 2, 15, R.string.level_childtimed, R.integer.level_childtimed_icon),
    Adult  (50, 0, 3, 5, R.string.level_adult, R.integer.level_adult_icon),
    AdultTimed  (50, 120, 3, 5, R.string.level_adulttimed, R.integer.level_adulttimed_icon)
    ;


    Mode(int numIcons, int timeAllowed, float overLap, int hints, int stringRes, int iconRes) {
        this.numIcons = numIcons;
        this.timeAllowed = timeAllowed;
        this.overLap = overLap;
        this.hints = hints;
        this.string = stringRes;
        this.icon = iconRes;
    }



    public int getNumIcons() {
        return numIcons;
    }

    public int getTimeAllowed() {
        return timeAllowed;
    }

    public boolean isTimed() {
        return timeAllowed>0;
    }

    public boolean showLevelComplete() {
        return timeAllowed!=-1;
    }

    public int getIconSize(int maxwidth){
        return  Math.max(maxwidth/16, Math.min(maxwidth/numIcons, 100));
    }

    public int getMinIconSize(int maxwidth) {
        return (int)(getIconSize(maxwidth)  *3.0/4);
    }

    public int getMaxIconSize(int maxwidth) {
        return getIconSize(maxwidth);
    }


    public int getMargin(int maxwidth) {
        return (int)(getIconSize(maxwidth) * 2.5);
    }

    public float getOverLap() {
        return overLap;
    }

    public int getHints() {
        return hints;
    }

    public boolean limitHints() {
        return hints>-1;
    }

    public String toString(Context context) {
        return new String(Character.toChars(context.getResources().getInteger(icon))) + "   " + context.getString(string);
    }


    private int numIcons;
    private float overLap;
    private int hints;
    private int timeAllowed;

    private int string;
    private int icon;



}
