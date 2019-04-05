package com.example.kevin.myapplication;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Color;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<DublinBikes> bikesArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        readCSVFile();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Bundle extras = getIntent().getExtras();
        Double latitude = extras.getDouble("latitude");
        Double longitude = extras.getDouble("longitude");
        String bikeStop = extras.getString("location");
        LatLng current = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(current).title("Current Location ").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        // Add a marker in Sydney and move the camera

        for(int i = 0; i<bikesArrayList.size(); i++) {
            double lat = bikesArrayList.get(i).getLatitude();
            double lon = bikesArrayList.get(i).getLongitude();
            if(bikesArrayList.get(i).getName().equals(bikeStop)){
                LatLng dublin = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(dublin).title(" " + bikesArrayList.get(i).getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(dublin));
                Polyline line = googleMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(latitude, longitude), new LatLng(bikesArrayList.get(i).getLatitude(),bikesArrayList.get(i).getLongitude() ))
                        .width(5)
                        .color(Color.BLACK));
            }
            else {
                LatLng dublin = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(dublin).title(" " + bikesArrayList.get(i).getName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(dublin));
            }
        }


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
}
