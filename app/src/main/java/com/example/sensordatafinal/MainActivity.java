package com.example.sensordatafinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //SensorManager object containing all the sensor services
    private SensorManager sensorManager;
    //Individual sensor objects
    private Sensor light;

    /* List of other sensors available for future enhancement
    private Sensor proximity;
    private Sensor magneto;
    private Sensor orientation;
    private Sensor gravity;
    private Sensor linearAcceleration;
    private Sensor stepCount;
    private Sensor stepDetector;
    */
    // TextViews to display current sensor values
    private TextView lightText;

    /*Text views for remaining sensors
    private TextView proximityText;
    private TextView magnetoText;
    private TextView orientationText;
    private TextView gravityText;
    private TextView linearAccelerationText;
    private TextView stepCountText;
    private TextView stepDetectorText;
    */

    private FileWriter writer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Writing Log to filesystem
        if (isStoragePermissionGrated()) {
            try {
                createLogFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            requestForStoragePermission();
        }

        // Get the sensor service and retrieve the list of sensors.
        sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        lightText = (TextView) findViewById(R.id.label_light);

        /*
        proximityText = (TextView) findViewById(R.id.label_proximity);
        magnetoText = (TextView) findViewById(R.id.label_magneto);
        gravityText = (TextView) findViewById(R.id.label_gravity);
        linearAccelerationText = (TextView) findViewById(R.id.label_linearacc);
        orientationText = (TextView) findViewById(R.id.label_orientation);
        stepCountText = (TextView) findViewById(R.id.label_stepcounter);
        stepDetectorText = (TextView) findViewById(R.id.label_stepdetector);
        */
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        /*
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        stepCount = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        magneto = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        */

        String sensor_error = getResources().getString(R.string.error_no_sensor);
        if (light == null) {
            lightText.setText(sensor_error);
        }
        /*
        if (mSensorProximity == null) {
            mTextSensorProximity.setText(sensor_error);
        }
        if (mSensorGravity == null) {
            mTextGravitySensor.setText(sensor_error);
        }
        if (mSensorLinearAcc == null) {
            mTextLinearAcceleration.setText(sensor_error);
        }
        if (mSensorStepCount == null) {
            mTextStepCounter.setText(sensor_error);
        }
        if (mSensorStepDetector == null) {
            mTextStepDetector.setText(sensor_error);
        }
        if (mSensorOrientation == null) {
            mTextOrientationSensor.setText(sensor_error);
        }
        if (mSensorMagneto == null) {
            mTextMagnetoMeter.setText(sensor_error);
        }
         */
    }

    private void requestForStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
    }

    private boolean isStoragePermissionGrated() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            try {
                createLogFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createLogFile() throws IOException {
        // create app folder
        File appDirectory = new File(Environment.getExternalStorageDirectory().toString(), "SensorData");
        System.out.println(appDirectory);
        if (!appDirectory.exists()) {
            System.out.println("Logs directory does not exist, creating new");
            appDirectory.mkdir();
        }

        // create log folder
        File logDirectory = new File(appDirectory, "Logs");
        if (!logDirectory.exists()) {
            System.out.println("Logs directory does not exist, creating new");
            logDirectory.mkdir();
        }
        writer = new FileWriter(new File(Environment.getExternalStorageDirectory(), "sensors_" + System.currentTimeMillis() + ".csv"));
        File logFile = new File(logDirectory, "logcat_" + System.currentTimeMillis() + ".csv");
//        try {
//            Process process = Runtime.getRuntime().exec("logcat -c");
//            process = Runtime.getRuntime().exec("logcat -f " + logFile);
//        } catch ( IOException e ) {
//            e.printStackTrace();
//        }
        System.out.println(logFile);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // The sensor type (as defined in the Sensor class).
        int sensorType = event.sensor.getType();

        // The new data value of the sensor.  Both the light and proximity
        // sensors report one value at a time, which is always the first
        // element in the values array.
        float currentValue = event.values[0];

        switch (sensorType) {
            case Sensor.TYPE_LIGHT:
                lightText.setText(getResources().getString(R.string.label_light, currentValue));
                Log.i("Light Sensor", String.valueOf(currentValue));
                try {
                    writer.write(String.format("%d, LIGHT, %f\n", event.timestamp, event.values[0], event.values[1]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            /*
            case Sensor.TYPE_PROXIMITY:
                proximityText.setText(getResources().getString(R.string.label_proximity, currentValue));
                Log.i("Proximity Sensor", String.valueOf(currentValue));
                break;
            case Sensor.TYPE_GRAVITY:
                gravityText.setText(getResources().getString(R.string.label_proximity, currentValue));
                Log.i("Gravity Sensor", String.valueOf(currentValue));
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearAccelerationText.setText(getResources().getString(R.string.label_linearacc, currentValue));
                Log.i("Linear Acceleration", String.valueOf(currentValue));
                break;
            case Sensor.TYPE_STEP_COUNTER:
                stepCountText.setText(getResources().getString(R.string.label_stepcounter, currentValue));
                Log.i("Step Counter", String.valueOf(currentValue));
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                stepDetectorText.setText(getResources().getString(R.string.label_stepdetector, currentValue));
                Log.i("Step Detector", String.valueOf(currentValue));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetoText.setText(getResources().getString(R.string.label_magneto, currentValue));
                Log.i("Magnetometer", String.valueOf(currentValue));
                break;
            case Sensor.TYPE_ORIENTATION:
                orientationText.setText(getResources().getString(
                        R.string.label_orientation, currentValue));
                Log.i("Motion Detector", String.valueOf(currentValue));
                break;
            */
            default:
                // do nothing
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (light != null) {
            sensorManager.registerListener(this, light,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        /*
        if (proximity != null) {
            sensorManager.registerListener(this, proximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (orientation != null) {
            sensorManager.registerListener(this, orientation,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gravity != null) {
            sensorManager.registerListener(this, gravity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (linearAcceleration != null) {
            sensorManager.registerListener(this, linearAcceleration,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (stepCount != null) {
            sensorManager.registerListener(this, stepCount,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (magneto != null) {
            sensorManager.registerListener(this, magneto,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
         */
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

}