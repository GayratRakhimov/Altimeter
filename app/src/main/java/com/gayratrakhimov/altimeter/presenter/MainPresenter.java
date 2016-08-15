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

    Context context;
    Activity activity;
    MainView mainView;

    // main variable
    // air pressure is saved in millibars
    double millibars;

    // calibration ration for air pressure in millibars
    // alt2>alt1, calibrationRatio<1
    // alt2<alt1, calibrationRatio>1
    double calibrationRatio = 1.0;

    // when millibars is min, altitude is max
    double minMillibarsWithoutCalibration = 1000d;
    // when millibars is max, altitude is min
    double maxMillibarsWithoutCalibration = 0d;

    // if true - draw graph, else stop drawing graph
    boolean trackingStarted = true;
    int changeCount = 0;

    // all data (timestamp, millibar) saved in array
    ArrayList<Data> history = new ArrayList<>();
    // entries for drawing graph
    ArrayList<Entry> entries = new ArrayList<Entry>();

    public MainPresenter(Context context, Activity activity, MainView mainView) {
        this.context = context;
        this.activity = activity;
        this.mainView = mainView;
        // get saved calibration ratio
        calibrationRatio = SP.getSharedPreferenceDouble(context, SP.CALIBRATION_RATIO, 1);
    }

    public void init() {
        initPressureListener();
    }

    // register air pressure listener
    private void initPressureListener() {
        SensorManager manager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        manager.registerListener(pressureListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // this listener catches changes in barometer value
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

    // update UI
    private void updateInfo() {

        // calibrated millibars
        double millibarsCalibrated = millibars * calibrationRatio;

        // when millibar is min, altitude is max
        if (millibars < minMillibarsWithoutCalibration) {
            // set min millibar
            minMillibarsWithoutCalibration = millibars;
            // show max altitude
            double altitudeInMeters = Converter.mbToMeter(millibarsCalibrated);
            mainView.setMaxAltitude((int) Utils.round(altitudeInMeters, 0) + "m");
            // set max limit for graph: altitude+5
            mainView.setMaxAxisValue((float) (Utils.round(altitudeInMeters, 0) + 5));
        }

        // when millibar is max, altitude is min
        if (millibars > maxMillibarsWithoutCalibration) {
            // set max millibar
            maxMillibarsWithoutCalibration = millibars;
            // show min altitude
            double altitudeInMeters = Converter.mbToMeter(millibarsCalibrated);
            mainView.setMinAltitude((int) Utils.round(altitudeInMeters, 0) + "m");
            // set min limit for graph: altitude-5
            mainView.setMinAxisValue((float) (Utils.round(altitudeInMeters, 0) - 5));
        }

        // show altitude in METERS
        double altitudeInMeters = Converter.mbToMeter(millibarsCalibrated);
        String altitudeInMetersText = (int) Utils.round(altitudeInMeters, 0) + " m";
        mainView.setAltitude(altitudeInMetersText);

        // show altitude in FEET
        double altitudeInFeet = Converter.mbToFt(millibarsCalibrated);
        String altitudeInFeetText = (int) Utils.round(altitudeInFeet, 0) + " ft";
        mainView.setAlternativeAltitude(altitudeInFeetText);

        // show MILLIBARS
        String millibarsText = (int) Utils.round(millibarsCalibrated, 0) + " mbar";
        mainView.setMillibars(millibarsText);

        // show KILOPASCALS
        double kilopascals = Converter.mbToKpa(millibarsCalibrated);
        String kilopascalsText = (int) Utils.round(kilopascals, 0) + " kPa";
        mainView.setKilopascals(kilopascalsText);

        // add point to graph
        long timestamp = (System.currentTimeMillis() / 1000) % 86400;
        Data data = new Data(timestamp, millibars);
        history.add(data);
        addEntry(data);

    }

    private void addEntry(Data data) {

        // calibrated altitude in meters
        float altitudeInMeters = (float) Converter.mbToMeter(data.getMillibar() * calibrationRatio);
        entries.add(new Entry(entries.size(), altitudeInMeters));

        // if graph is new, create it
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

            // set data
            mainView.setChartData(lineData);
        }
    }

    // menu item clicked
    public void onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                trackingStarted = !trackingStarted;
                if (trackingStarted) {//resume
                    item.setIcon(R.drawable.ic_pause_circle_filled_white_24dp);
                    mainView.showToast(context.getString(R.string.graph_animation_resumed));
                } else {//pause
                    item.setIcon(R.drawable.ic_play_circle_filled_white_24dp);
                    mainView.showToast(context.getString(R.string.graph_animation_paused));
                }
                break;
        }
    }

    // all view clicks comes here
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.altitudeMeters://show calibration in meters
                double altitudeInMeters = Converter.mbToMeter(millibars * calibrationRatio);
                showCalibrateDialog(CalibrationUnit.METER, (int) Utils.round(altitudeInMeters, 0));
                break;
            case R.id.altitudeFeet://show calibration in feet
                double altitudeInFeet = Converter.mbToFt(millibars * calibrationRatio);
                showCalibrateDialog(CalibrationUnit.FOOT, (int) Utils.round(altitudeInFeet, 0));
                break;
        }
    }

    // calibrated
    public void onCalibrateOkButton(CalibrationUnit calibrationUnit, int altitude) {

        // identify ratio
        if (calibrationUnit == CalibrationUnit.METER) {
            double calibratedMillibars = Converter.metersToMb(altitude);
            calibrationRatio = calibratedMillibars / millibars;
        } else if (calibrationUnit == CalibrationUnit.FOOT) {
            double calibratedMillibars = Converter.ftToMb(altitude);
            calibrationRatio = calibratedMillibars / millibars;
        }

        // save ration
        SP.setSharedPreferenceDouble(context, SP.CALIBRATION_RATIO, calibrationRatio);

        // re-enter calibrated values
        entries = new ArrayList<>();
        for (Data data : history) {
            addEntry(data);
        }

        // update max calibrated altitude
        double maxAltitudeInMeters = Converter.mbToMeter(minMillibarsWithoutCalibration*calibrationRatio);
        mainView.setMaxAltitude((int) Utils.round(maxAltitudeInMeters, 0) + "m");
        mainView.setMaxAxisValue((float) (Utils.round(maxAltitudeInMeters, 0) + 5));

        // update min calibrated altitude
        double minAltitudeInMeters = Converter.mbToMeter(maxMillibarsWithoutCalibration*calibrationRatio);
        mainView.setMinAltitude((int) Utils.round(minAltitudeInMeters, 0) + "m");
        mainView.setMinAxisValue((float) (Utils.round(minAltitudeInMeters, 0) - 5));

    }

    private void showCalibrateDialog(CalibrationUnit calibrationUnit, int value) {

        String title = context.getString(R.string.calibrate_meters);
        int maxValue = (int) Const.TROPOSPHERE_ALTITUDE_IN_METERS;
        if (calibrationUnit == CalibrationUnit.FOOT) {
            title = context.getString(R.string.calibrate_feet);
            maxValue = (int) Const.TROPOSPHERE_ALTITUDE_IN_FEET;
        }
        mainView.showCalibrateAltitudeDialog(calibrationUnit, title, value, maxValue);

    }

}
