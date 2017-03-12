package quaap.com.dodatheexploda;

import android.content.Context;

/**
 * Created by tom on 3/11/17.
 */

public enum Mode {

    Baby   (3, 0, 1, -1, R.string.level_baby, R.integer.level_baby_icon),
    Toddler(7, 0, 2, -1, R.string.level_toddler, R.integer.level_toddler_icon),
    Child  (25, 0, 3, 15, R.string.level_child, R.integer.level_child_icon),
    ChildTimed  (25, 180, 3, 15, R.string.level_childtimed, R.integer.level_childtimed_icon),
    Adult  (50, 0, 4, 5, R.string.level_adult, R.integer.level_adult_icon),
    AdultTimed  (50, 12, 4, 5, R.string.level_adulttimed, R.integer.level_adulttimed_icon)
    ;


    Mode(int numIcons, int timeAllowed, int overLap, int hints, int stringRes, int iconRes) {
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
        return timeAllowed!=0;
    }

    public int getIconSize(int maxwidth){
        return  Math.max(40, (int)Math.min(maxwidth/(numIcons), 100));
    }

    public int getMinIconSize(int maxwidth) {
        return getIconSize(maxwidth)  *2/3;
    }

    public int getMaxIconSize(int maxwidth) {
        return getIconSize(maxwidth);
    }


    public int getMargin(int maxwidth) {
        return getIconSize(maxwidth) * 2;
    }

    public int getOverLap() {
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
    private int overLap;
    private int hints;
    private int timeAllowed;

    private int string;
    private int icon;



}
