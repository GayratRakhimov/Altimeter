package com.gayratrakhimov.altimeter.activity;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gayratrakhimov.altimeter.R;
import com.gayratrakhimov.altimeter.models.CalibrationUnit;
import com.gayratrakhimov.altimeter.presenter.MainPresenter;
import com.gayratrakhimov.altimeter.view.MainView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class MainActivity extends AppCompatActivity implements MainView, View.OnClickListener, OnChartGestureListener, OnChartValueSelectedListener {

    MainPresenter mainPresenter;

    TextView millibars;
    TextView alternativeAltitude;
    TextView kilopascals;
    TextView altitude;
    TextView minAltitude;
    TextView minAltitudeFeet;
    TextView maxAltitude;
    TextView maxAltitudeFeet;
    CardView altitudeMeters;
    CardView altitudeFeet;
    Button defaultCalibration;

    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        altitude = (TextView) findViewById(R.id.altitude);
        alternativeAltitude = (TextView) findViewById(R.id.alternative_altitude);
        millibars = (TextView) findViewById(R.id.millibars);
        kilopascals = (TextView) findViewById(R.id.kilopascals);
        minAltitude = (TextView) findViewById(R.id.minAltitude);
        minAltitudeFeet = (TextView) findViewById(R.id.minAltitudeFeet);
        maxAltitude = (TextView) findViewById(R.id.maxAltitude);
        maxAltitudeFeet = (TextView) findViewById(R.id.maxAltitudeFeet);
        altitudeMeters = (CardView) findViewById(R.id.altitudeMeters);
        altitudeMeters.setOnClickListener(this);
        altitudeFeet = (CardView) findViewById(R.id.altitudeFeet);
        altitudeFeet.setOnClickListener(this);
        defaultCalibration = (Button) findViewById(R.id.default_calibration);
        defaultCalibration.setOnClickListener(this);

        setupChart();

        mainPresenter = new MainPresenter(getApplicationContext(), MainActivity.this, this);
        mainPresenter.init();

    }

    private void setupChart() {

        chart = (LineChart) findViewById(R.id.chart);
        chart.setOnChartGestureListener(this);
        chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(false);

        // no description text
        chart.setDescription("");
        chart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        chart.getLegend().setEnabled(false);

        // set an alternative background color
        chart.setBackgroundColor(0xFF25252F);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//
//        // set the marker to the chart
//        chart.setMarkerView(mv);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(0xFF7E7E7E);
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long timestamp = (long) value;
                Date date = new Date(timestamp * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("ss");
                return String.valueOf(sdf.format(date));
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(0xFF7E7E7E);
        leftAxis.setTextSize(12f);
        leftAxis.removeAllLimitLines();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMaxValue(510f);
        leftAxis.setAxisMinValue(490f);
        //leftAxis.setYOffset(20f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        chart.getAxisRight().setEnabled(false);
    }

    @Override
    public void setAltitude(String info) {
        altitude.setText(info);
    }

    @Override
    public void setMinAltitude(String info) {
        minAltitude.setText(info);
    }

    @Override
    public void setMinAltitudeFeet(String info) {
        minAltitudeFeet.setText(info);
    }

    @Override
    public void setMaxAltitude(String info) {
        maxAltitude.setText(info);
    }

    @Override
    public void setMaxAltitudeFeet(String info) {
        maxAltitudeFeet.setText(info);
    }

    @Override
    public void setAlternativeAltitude(String info) {
        alternativeAltitude.setText(info);
    }

    @Override
    public void setMillibars(String info) {
        millibars.setText(info);
    }

    @Override
    public void setKilopascals(String info) {
        kilopascals.setText(info);
    }

    @Override
    public void onClick(View view) {
        mainPresenter.onClick(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mainPresenter.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void setChartData(LineData data) {
        chart.setData(data);
    }

    @Override
    public void setMinAxisValue(float minAxisValue) {
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinValue(minAxisValue);
    }

    @Override
    public void setMaxAxisValue(float maxAxisValue) {
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaxValue(maxAxisValue);
    }

    @Override
    public void notifyDataSetChanged() {
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    @Override
    public LineDataSet getChartDataSet() {
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0)
            return (LineDataSet) chart.getData().getDataSetByIndex(0);
        return null;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            chart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + chart.getLowestVisibleX() + ", high: " + chart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + chart.getXChartMin() + ", xmax: " + chart.getXChartMax() + ", ymin: " + chart.getYChartMin() + ", ymax: " + chart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    public void showCalibrateAltitudeDialog(final CalibrationUnit calibrationUnit, String title, final int value, int maxValue) {

        final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(this)
                .minValue(0)
                .maxValue(maxValue)
                .defaultValue(value)
                .backgroundColor(Color.WHITE)
                .separatorColor(Color.TRANSPARENT)
                .textColor(Color.BLACK)
                .textSize(20)
                .enableFocusability(false)
                .wrapSelectorWheel(false)
                .build();

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(numberPicker)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainPresenter.onCalibrateOkButton(calibrationUnit, numberPicker.getValue());
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showAlertDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialogBuilder.show();
    }
}
