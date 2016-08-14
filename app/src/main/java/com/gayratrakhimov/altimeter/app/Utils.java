package com.gayratrakhimov.altimeter.app;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by User on 07.08.2016.
 */
public class Utils {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
