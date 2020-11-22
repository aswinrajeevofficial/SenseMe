package com.example.sensordatafinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.format.DateFormat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    private FileWriter writer;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonOne = findViewById(R.id.buttonOne);
        Button buttonTwo = findViewById(R.id.buttonTwo);

        buttonOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Relay Sensor Data Clicked");
                Intent sensorActivity = new Intent(getApplicationContext(), SensorActivity.class);
                startActivity(sensorActivity);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        buttonTwo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Get Open Data Clicked");

                SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
                Date d = new Date();
                String day = (String) DateFormat.format("dd",   d); // 20
                String monthNumber = (String) DateFormat.format("MM",   d); // 06
                String year = (String) DateFormat.format("yyyy", d); // 2013
                String hour = (String) DateFormat.format("HH", d);
                String minute = (String) DateFormat.format("mm", d);
                String seconds = (String) DateFormat.format("ss", d);
                System.out.println(day + " " + monthNumber + " " + " " + year + " " + hour + ":" + minute + ":" + seconds);
                String fileName = "open_data" + year + monthNumber + day + hour + minute + seconds + ".json";
                String url ="https://api.data.gov.sg/v1/environment/air-temperature?" + year + "-" + monthNumber + "-" + day + "T" + hour + ":" + minute + ":" + seconds;
                // prepare the Request
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d("Response", response.toString());
                                try {
                                    writer = new FileWriter(new File(Environment.getExternalStorageDirectory(), fileName));
                                    writer.write(response.toString());
                                    writer.close();
                                    Toast.makeText(getApplicationContext(), "Open data saved", Toast.LENGTH_LONG).show();
                                    storageRef = FirebaseStorage.getInstance().getReference();
                                    Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),fileName));
                                    StorageReference toUpload = storageRef.child(fileName);

                                    toUpload.putFile(file)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    System.out.println("File uploaded successfully");
                                                    Toast.makeText(getApplicationContext(),"Open data uploaded to Firebase successfully",Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(getApplicationContext(),"Open data was not uploaded to Firebase successfully",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error.Response", error.toString());
                            }
                        }
                );
                // add it to the RequestQueue
                queue.add(getRequest);
            }
        });
    }

}