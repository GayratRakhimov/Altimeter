package com.gayratrakhimov.altimeter.view;


import com.gayratrakhimov.altimeter.models.CalibrationUnit;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * Created by User on 07.08.2016.
 */
public interface MainView {

    void setMinAltitude(String info);
    void setMaxAltitude(String info);
    void setAltitude(String info);
    void setAlternativeAltitude(String info);
    void setMillibars(String info);
    void setKilopascals(String info);
    void setChartData(LineData data);
    void notifyDataSetChanged();
    LineDataSet getChartDataSet();
    void setMinAxisValue(float minAxisValue);
    void setMaxAxisValue(float maxAxisValue);
    void showCalibrateAltitudeDialog(CalibrationUnit calibrationUnit, String title, int value, int maxValue);
    void showToast(String toast);

}
