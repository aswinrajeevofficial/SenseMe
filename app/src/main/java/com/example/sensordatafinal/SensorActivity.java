package com.example.sensordatafinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    //SensorManager object containing all the sensor services
    private SensorManager sensorManager;
    //Individual sensor objects
    private Sensor light;
    // TextViews to display current sensor values
    private TextView lightText;
    private FileWriter writer;
    String fileName = "sensors_" + System.currentTimeMillis() + ".csv";
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_activity);
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
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        String sensor_error = getResources().getString(R.string.error_no_sensor);
        if (light == null) {
            lightText.setText(sensor_error);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
        storageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),fileName));
        StorageReference toUpload = storageRef.child(fileName);

        toUpload.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       System.out.println("File uploaded successfully");
                       Toast.makeText(getApplicationContext(),"File uploaded to Firebase successfully",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(),"File was not uploaded to Firebase successfully",Toast.LENGTH_LONG).show();
                    }
                });
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
        writer = new FileWriter(new File(Environment.getExternalStorageDirectory(), fileName));
    }


}