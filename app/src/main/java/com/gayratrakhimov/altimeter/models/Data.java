package com.gayratrakhimov.altimeter.models;

/**
 * Created by User on 14.08.2016.
 */
public class Data {

    long timestamp;
    double millibar;

    public Data() {
    }

    public Data(long timestamp, double millibar) {
        this.timestamp = timestamp;
        this.millibar = millibar;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getMillibar() {
        return millibar;
    }

    public void setMillibar(double millibar) {
        this.millibar = millibar;
    }
}
