package com.gayratrakhimov.altimeter.app;

/**
 * Created by User on 14.08.2016.
 */
public class Converter {

    public static double mbToFt(double millibars){
        return (1 - Math.pow(millibars / 1013.25, 0.190284)) * 145366.45;
    }

    public static double mbToKpa(double millibars){
        return millibars * Const.KILOPASCALS_IN_MILLIBAR;
    }

    public static double mbToMeter(double millibars){
        return mbToFt(millibars) * Const.METERS_IN_FOOT;
    }

    public static double ftToMb(double feet){
        return (Math.pow(1-feet / 145366.45, 5.255302600323727)) * 1013.25;
    }

    public static double metersToMb(double meters) {
        return ftToMb(meters/Const.METERS_IN_FOOT);
    }

}
