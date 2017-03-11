package quaap.com.dodatheexploda;

/**
 * Created by tom on 3/11/17.
 */

public enum Mode {

    Baby(3, 70, 100, 0),
    Toddler(10, 50, 80, 1),
    Child(40, 50, 80, 2),
    Adult(90, 20, 40, 4)
    ;


    Mode(int numIcons, int minIconSize, int maxIconSize, int overLap) {
        this.numIcons = numIcons;
        this.minIconSize = minIconSize;
        this.maxIconSize = maxIconSize;
        this.overLap = overLap;
    }



    public int getNumIcons() {
        return numIcons;
    }

    public int getMinIconSize() {
        return minIconSize;
    }

    public int getMaxIconSize() {
        return maxIconSize;
    }

    public int getMargin() {
        return maxIconSize * 2;
    }

    public int getOverLap() {
        return overLap;
    }

    private int numIcons;
    private int minIconSize;
    private int maxIconSize;
    private int overLap;



}
