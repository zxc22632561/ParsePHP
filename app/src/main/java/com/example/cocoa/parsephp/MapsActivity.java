package com.example.cocoa.parsephp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static final String TAG = "";

    

    ArrayList<LatLng> MarkerPoints;
    double d;
    double g ;

    ArrayList<Double> lat_a;   //lat_a
    ArrayList<Double> lat_b;   //lat_b
    ArrayList<String> name;
    ArrayList<Integer> no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getData();
        MarkerPoints = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                if (MarkerPoints.size() >= 0) {
                    MarkerPoints.clear();
                    mMap.clear();
                }
                MarkerPoints.add(point);
                mMap.addMarker(new MarkerOptions()
                        .position(point)   //B點資料
                        .title("目前位置")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                Button crash = findViewById(R.id.btn_crash);

                for (int i = 0 ; i < lat_a.size(); i++){

                    d = gps2m(point.latitude,point.longitude,lat_a.get(i),lat_b.get(i));
                    g = gps2m(lat_a.get(i),lat_b.get(i),point.latitude,point.longitude);

                    switch (i) {
                        case 0:
                            if (d < 100) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat_a.get(i), lat_b.get(i)))
                                        .title(name.get(i)));
                                crash.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(v.getContext(), "抓取成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (g > 100) {
                                crash.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(v.getContext(), "抓取失敗", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            break;
                        case 1:
                            if (d < 100) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat_a.get(i), lat_b.get(i)))
                                        .title(name.get(i)));

                                crash.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(v.getContext(), "抓取成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        case 2:
                            if (d < 100) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat_a.get(i), lat_b.get(i)))
                                        .title(name.get(i)));
                                crash.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(v.getContext(), "抓取成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            break;
                    }
                    Log.d("catch",name.get(i)+"/"+i+"/"+d+"/"+g);
                }
            }
        });
    }

    private void getData() {
        String urlParkingArea = "http://192.168.1.10/json.php";
        StringRequest stringRequest = new StringRequest(
                urlParkingArea,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response = " + response.toString());
                        parserJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "error : " + error.toString());
                    }
                }
        );
        Volley.newRequestQueue(this).add(stringRequest);
    }
    private void parserJson(String data) {
        lat_a = new ArrayList<>();
        lat_b = new ArrayList<>();
        name = new ArrayList<>();
        no = new ArrayList<>();
        try {
            JSONArray JA = new JSONArray(data);
            for (int i = 0; i < JA.length(); i++) {
                JSONObject JO = (JSONObject) JA.get(i);

                no.add(JO.getInt("no"));
                name.add(JO.getString("city"));

                lat_a.add(JO.getDouble("lat"));  //存到陣列
                lat_b.add(JO.getDouble("lon"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private final double EARTH_RADIUS = 6378137.0;
    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
    double radLat1 = (lat_a * Math.PI / 180.0);
    double radLat2 = (lat_b * Math.PI / 180.0);
    double a = radLat1 - radLat2;
    double b = (lng_a - lng_b) * Math.PI / 180.0;
    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                        + Math.cos(radLat1) * Math.cos(radLat2)
                        * Math.pow(Math.sin(b / 2), 2)));
    s = s * EARTH_RADIUS;
    s = Math.round(s * 10000) / 10000;
    return s;
    }
}


