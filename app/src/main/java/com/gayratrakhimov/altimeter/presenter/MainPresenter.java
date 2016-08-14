package com.gayratrakhimov.altimeter.presenter;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MenuItem;
import android.view.View;

import com.gayratrakhimov.altimeter.R;
import com.gayratrakhimov.altimeter.app.Const;
import com.gayratrakhimov.altimeter.app.Converter;
import com.gayratrakhimov.altimeter.app.Utils;
import com.gayratrakhimov.altimeter.data.SP;
import com.gayratrakhimov.altimeter.models.CalibrationUnit;
import com.gayratrakhimov.altimeter.models.Data;
import com.gayratrakhimov.altimeter.view.MainView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

/**
 * Created by User on 07.08.2016.
 */
public class MainPresenter {

    double calibrationRatio = 1.0;

    Context context;
    Activity activity;
    MainView mainView;

    double millibars;

    boolean trackingStarted = true;

    int changeCount = 0;

    double minMillibarsWithoutCalibration = 1000d;
    double maxMillibarsWithoutCalibration = 0d;

    ArrayList<Data> history = new ArrayList<>();
    ArrayList<Entry> entries = new ArrayList<Entry>();

    public MainPresenter(Context context, Activity activity, MainView mainView) {
        this.context = context;
        this.activity = activity;
        this.mainView = mainView;
        calibrationRatio = SP.getSharedPreferenceDouble(context, SP.CALIBRATION_RATIO, 1);
    }

    public void init() {
        initPressureListener();
    }

    private void initPressureListener() {
        SensorManager manager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        manager.registerListener(pressureListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values;
            millibars = values[0];
            if (trackingStarted && changeCount % 5 == 0) {
                updateInfo();
            }
            changeCount++;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    private void updateInfo() {

        double millibarsCalibrated = millibars * calibrationRatio;

        if (millibars < minMillibarsWithoutCalibration) {
            minMillibarsWithoutCalibration = millibars;
            double altitudeInMeters = Converter.mbToMeter(millibarsCalibrated);
            mainView.setMaxAltitude((int) Utils.round(altitudeInMeters, 0) + "m");
            mainView.setMaxAxisValue((float) (Utils.round(altitudeInMeters, 0) + 5));
        }

        if (millibars > maxMillibarsWithoutCalibration) {
            maxMillibarsWithoutCalibration = millibars;
            double altitudeInMeters = Converter.mbToMeter(millibarsCalibrated);
            mainView.setMinAltitude((int) Utils.round(altitudeInMeters, 0) + "m");
            mainView.setMinAxisValue((float) (Utils.round(altitudeInMeters, 0) - 5));
        }

        // meters
        double altitudeInMeters = Converter.mbToMeter(millibarsCalibrated);
        String altitudeInMetersText = (int) Utils.round(altitudeInMeters, 0) + " m";
        mainView.setAltitude(altitudeInMetersText);

        // feet
        double altitudeInFeet = Converter.mbToFt(millibarsCalibrated);
        String altitudeInFeetText = (int) Utils.round(altitudeInFeet, 0) + " ft";
        mainView.setAlternativeAltitude(altitudeInFeetText);

        // mbar
        String millibarsText = (int) Utils.round(millibarsCalibrated, 0) + " mbar";
        mainView.setMillibars(millibarsText);

        // kPa
        double kilopascals = Converter.mbToKpa(millibarsCalibrated);
        String kilopascalsText = (int) Utils.round(kilopascals, 0) + " kPa";
        mainView.setKilopascals(kilopascalsText);


        long timestamp = (System.currentTimeMillis() / 1000) % 86400;
        Data data = new Data(timestamp, millibars);
        history.add(data);
        addEntry(data);

    }

    private void addEntry(Data data) {

        float altitudeInMeters = (float) Converter.mbToMeter(data.getMillibar()*calibrationRatio);
        entries.add(new Entry(entries.size(), altitudeInMeters));

        LineDataSet set1 = mainView.getChartDataSet();
        if (set1 != null) {
            set1.setValues(entries);
            mainView.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(entries, "DataSet 1");

            set1.setLineWidth(1.75f);
            set1.setCircleRadius(1f);
            set1.setColor(0xFFC93DD4);
            set1.setCircleColor(0xFFFFFFFD);
            set1.setCircleColorHole(Color.rgb(89, 199, 250));
            set1.setDrawValues(false);
            set1.setDrawCircleHole(false);
            set1.setDrawFilled(false);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData lineData = new LineData(dataSets);

            // set data]
            mainView.setChartData(lineData);
        }
    }

    public void onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                trackingStarted = !trackingStarted;
                if (trackingStarted) item.setIcon(R.drawable.ic_pause_circle_filled_white_24dp);
                else item.setIcon(R.drawable.ic_play_circle_filled_white_24dp);
                break;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.altitudeMeters:
                double altitudeInMeters = Converter.mbToMeter(millibars*calibrationRatio);
                showCalibrateDialog(CalibrationUnit.METER, (int) Utils.round(altitudeInMeters, 0));
                break;
            case R.id.altitudeFeet:
                double altitudeInFeet = Converter.mbToFt(millibars*calibrationRatio);
                showCalibrateDialog(CalibrationUnit.FOOT, (int) Utils.round(altitudeInFeet, 0));
                break;
        }
    }

    public void onCalibrateOkButton(CalibrationUnit calibrationUnit, int altitude) {

        if (calibrationUnit == CalibrationUnit.METER) {
            double calibratedMillibars = Converter.metersToMb(altitude);
            calibrationRatio = calibratedMillibars/millibars;
        } else if (calibrationUnit == CalibrationUnit.FOOT) {
            double calibratedMillibars = Converter.ftToMb(altitude);
            calibrationRatio = calibratedMillibars/millibars;
        }

        minMillibarsWithoutCalibration = 1000d;
        maxMillibarsWithoutCalibration = 0d;
        entries = new ArrayList<>();
        for(Data data: history){
            addEntry(data);
        }

    }

    private void showCalibrateDialog(CalibrationUnit calibrationUnit, int value) {

        String title = "Calibrate(in meters)";
        int maxValue = (int) Const.TROPOSPHERE_ALTITUDE_IN_METERS;
        if (calibrationUnit == CalibrationUnit.FOOT) {
            title = "Calibrate(in feet)";
            maxValue = (int) Const.TROPOSPHERE_ALTITUDE_IN_FEET;
        }
        mainView.showCalibrateAltitudeDialog(calibrationUnit, title, value, maxValue);

    }

}
