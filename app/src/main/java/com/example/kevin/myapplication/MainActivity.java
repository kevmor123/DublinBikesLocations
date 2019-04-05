package com.example.kevin.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;



import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity{
    FusedLocationProviderClient locationClient;
    LocationLongLat currentLocation = new LocationLongLat();
    ArrayList<DublinBikes> bikesArrayList = new ArrayList<>();
    DatabaseReference newRef;
    DatabaseReference mRootRef;
    DatabaseReference mConditionRef;
    Button button;
    Button buttonMap;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        text = (TextView) findViewById((R.id.textView1));
        button = (Button) findViewById(R.id.button);
        buttonMap = (Button) findViewById(R.id.buttonMap);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mConditionRef = mRootRef.child("bikeLocations");
        permissionRequest();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        readCSVFile();
        getLocationLongLat();

        final long period = 15000;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                returnLocation();
            }
        }, 0, period);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mConditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeCheck()){
                    text.setText(" Longitude = " + currentLocation.getLongitude() + "\n Latitude = "
                            + currentLocation.getLatitude() + "\n Closest bike depot = " + currentLocation.getBikeLocation() + "\n It's getting late, don't forget to put the bike lights on and stay safe!");
                }else{
                    text.setText(" Longitude = " + currentLocation.getLongitude() + "\n Latitude = "
                            + currentLocation.getLatitude() + "\n Closest bike depot = " + currentLocation.getBikeLocation());
                }
            }
        });

        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("latitude", currentLocation.getLatitude());
                intent.putExtra("location", currentLocation.getBikeLocation());
                intent.putExtra("longitude", currentLocation.getLongitude());// getText() SHOULD NOT be static!!!
                startActivity(intent);
            }
        });
    }

    private boolean timeCheck(){
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return currentHour >= 17;
    }

    private void permissionRequest(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    private void returnLocation(){
        if (ActivityCompat.checkSelfPermission( MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    String closestBike = nearestBikeLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    LocationLongLat newLocation = new LocationLongLat(location.getLongitude(), location.getLatitude(), closestBike);
                    newRef = mConditionRef.push();
                    newRef.setValue(newLocation);
                    currentLocation.setLatitude(newLocation.getLatitude());
                    currentLocation.setLongitude(newLocation.getLongitude());
                    currentLocation.setBikeLocation(newLocation.getBikeLocation());
                }
            }
        });
    }

    private void readCSVFile(){
        InputStream file = getResources().openRawResource(R.raw.dccdublinbikesstationsgpscoords);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file, Charset.forName("UTF-8"))
        );

        String line = "";

        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            while((line = reader.readLine()) != null){
                String[] tokens = line.split(",");
                DublinBikes currentStop = new DublinBikes();
                currentStop.setName(tokens[0]);
                currentStop.setLatitude(Double.parseDouble(tokens[1]));
                currentStop.setLongitude(Double.parseDouble(tokens[2]));
                bikesArrayList.add(currentStop);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getLocationLongLat(){
        if (ActivityCompat.checkSelfPermission( MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    LocationLongLat newLocation = new LocationLongLat(location.getLongitude(), location.getLatitude(), "");
                    currentLocation.setLatitude(newLocation.getLatitude());
                    currentLocation.setLongitude(newLocation.getLongitude());
                }
            }
        });
    }

    private double distanceCalc(double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }

    private String nearestBikeLocation(double latOfDevice, double longOfDevice){
        double shortestDistance = 10000000;
        String shortestBikeStop = "";
        double currentDistance;

        for(int i = 0; i<bikesArrayList.size(); i++) {
            double lat = bikesArrayList.get(i).getLatitude();
            double lon = bikesArrayList.get(i).getLongitude();

            currentDistance = distanceCalc(lat, lon, latOfDevice, longOfDevice);
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance;
                shortestBikeStop = bikesArrayList.get(i).getName();
            }
        }
        return shortestBikeStop;
    }
}
