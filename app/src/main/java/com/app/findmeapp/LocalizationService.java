package com.app.findmeapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.app.findmeapp.model.Coordinates;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ioana on 24-Mar-18.
 */

public class LocalizationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle("Location service activated")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_launcher_big)
                        .build();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        Toast.makeText(getApplicationContext(), "Location service started", Toast.LENGTH_SHORT).show();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                writeNewLocation(location.getLongitude(), location.getLatitude(), Calendar.getInstance().getTime().getTime());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private void writeNewLocation(Double longitude, Double latitude, Long timestamp) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Coordinates coordinates = new Coordinates(longitude, latitude, timestamp);
            mDatabase.child("users").child(user.getUid()).child("coordinates").setValue(coordinates);
        }
    }
}