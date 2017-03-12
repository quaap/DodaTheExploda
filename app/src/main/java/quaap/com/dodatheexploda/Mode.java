package quaap.com.dodatheexploda;

import android.content.Context;

/**
 * Created by tom on 3/11/17.
 */

public enum Mode {

    Baby   (3, 240, 1, 100),
    Toddler(7, 240, 2, 100),
    Child  (25, 180, 3, 15),
    Adult  (50, 128, 4, 5)
    ;


    Mode(int numIcons, int timeAllowed, int overLap, int hints) {
        this.numIcons = numIcons;
        this.timeAllowed = timeAllowed;
        this.overLap = overLap;
        this.hints = hints;
    }



    public int getNumIcons() {
        return numIcons;
    }

    public int getTimeAllowed() {
        return timeAllowed;
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

    private int numIcons;
    private int overLap;
    private int hints;
    private int timeAllowed;




}
