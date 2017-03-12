package quaap.com.dodatheexploda;

import android.content.Context;

/**
 * Created by tom on 3/11/17.
 */

public enum Mode {

    Baby   (3,  1, 100),
    Toddler(7,  2, 100),
    Child  (25, 3, 15),
    Adult  (50, 4, 5)
    ;


    Mode(int numIcons, int overLap, int hints) {
        this.numIcons = numIcons;
        this.overLap = overLap;
        this.hints = hints;
    }



    public int getNumIcons() {
        return numIcons;
    }

    public int getIconSize(int maxwidth){
        return  Math.max(36, (int)Math.min(maxwidth/(numIcons), 100));
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




}
