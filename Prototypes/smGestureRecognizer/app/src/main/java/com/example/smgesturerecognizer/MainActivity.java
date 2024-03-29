package com.example.smgesturerecognizer;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Date;
import java.lang.Object;

import com.example.smgesturerecognizer.dtw.FastDTW;
import com.example.smgesturerecognizer.dtw.TimeWarpInfo;
import com.example.smgesturerecognizer.timeseries.CustomizedGestureData;
import com.example.smgesturerecognizer.timeseries.TimeSeries;
import com.example.smgesturerecognizer.timeseries.TimeSeriesPoint;
import com.example.smgesturerecognizer.utils.DistanceFunction;
import com.example.smgesturerecognizer.utils.DistanceFunctionFactory;

import com.example.smgesturerecognizer.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;

    private TextView txtFirstPass;
    private TextView txtOutput;
    private Button btnStart;
    private Button btnEnd;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private boolean firstPass = true;

    private TimeSeries timeSeries;
    private TimeSeries secondTimeSeries;

    private long startTimestamp;
    private double firstPassTimeInterval;

    CustomizedGestureData custom = new CustomizedGestureData();
    private int idx1 = 0, idx2 = 0;

    private SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            /*
            float xValue = sensorEvent.values[0];
            float yValue = sensorEvent.values[1];
            float zValue = sensorEvent.values[2];

            double[] values = {xValue, yValue, zValue};
            long time = sensorEvent.timestamp - startTimestamp;

            if (firstPass) {
                timeSeries.addLast(time, new TimeSeriesPoint(values));
            } else {
                secondTimeSeries.addLast(time, new TimeSeriesPoint(values));
            }

            Log.d("MainActivity", xValue + " " + yValue + " " + zValue);

             */

            long time = sensorEvent.timestamp - startTimestamp;;

            double[][] val1 = custom.getCustTimeSeries();
            double[][] val2 = custom.getCustSecondTimeSeries();

            if (firstPass) {
                if(idx1 < val1.length){
                    timeSeries.addLast(time, new TimeSeriesPoint(val1[idx1]));
                    idx1 += 1;
                }
            } else {
                if(idx2 < val2.length){
                    secondTimeSeries.addLast(time, new TimeSeriesPoint(val2[idx2]));
                    idx2 += 1;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        txtFirstPass = findViewById(R.id.txt_first_pass);
        txtOutput = findViewById(R.id.txtOutput);
        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGesture();
            }
        });
        btnEnd = findViewById(R.id.btn_stop);
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGesture();
            }
        });

        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Toast.makeText(this, "Cheese is broken", Toast.LENGTH_SHORT).show();
        }
    }

    private void startGesture() {
        if (firstPass) {
            timeSeries = new TimeSeries(3);
            secondTimeSeries = new TimeSeries(3);
        }

        btnStart.setEnabled(false);
        btnEnd.setEnabled(true);
        Date d = new Date();
        startTimestamp = d.getTime();
        mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        /*For custom data (not working properly yet)
        long time = d.getTime() - startTimestamp;;
        double[][] val1 = custom.getCustTimeSeries();
        double[][] val2 = custom.getCustSecondTimeSeries();

        if (firstPass) {
            for(int i=0; i<val1.length; i++){
                timeSeries.addLast(time, new TimeSeriesPoint(val1[i]));
            }
        } else {
            for(int i=0; i<val2.length; i++){
                secondTimeSeries.addLast(time, new TimeSeriesPoint(val2[i]));
            }
        }

         */
    }

    private void endGesture() {
        mSensorManager.unregisterListener(mSensorListener);
        btnStart.setEnabled(true);
        btnEnd.setEnabled(false);

        if (firstPass) {
            txtFirstPass.setText("First pass done with " + timeSeries.numOfPts() + " points");
            firstPass = false;
            long endTimestamp = new Date().getTime();
            firstPassTimeInterval = (endTimestamp - startTimestamp) / 1000.0F;
        } else {
            DistanceFunction distFunc = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
            TimeWarpInfo info = FastDTW.getWarpInfoBetween(timeSeries, secondTimeSeries, 10, distFunc);

            long endTimestamp = new Date().getTime();
            double secondsInterval = (endTimestamp - startTimestamp) / 1000.0F;
            double avgSecondsInterval = (secondsInterval + firstPassTimeInterval) / 2;
            String seconds = String.format("%.2f", avgSecondsInterval);
            double distancePerSecond = info.getDistance() / avgSecondsInterval;
            String match = (distancePerSecond < 390) ? "✔" : "✗";

            firstPass = true;
            String output = txtOutput.getText().toString();
            output = output + " m:" + match + " d: " + info.getDistance() + " - t:" + seconds + " \n ";
            txtOutput.setText(output);

            timeSeries.clear();
            secondTimeSeries.clear();
        }
    }
}