package quaap.com.dodatheexploda;

/**
 * Created by tom on 3/11/17.
 */

public enum Mode {

    Baby(3, 90, 120, 1),
    Toddler(7, 70, 90, 2),
    Child(30, 50, 80, 3),
    Adult(90, 30, 50, 4)
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

    public int getBigSize() {
        return (int) (maxIconSize * 1.5);
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
