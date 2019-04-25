package com.example.endof433;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FindClubsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private SQLiteDatabase db;
    private String[] clubNames;
    private String[] locationNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findclub);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        db = this.openOrCreateDatabase("MyDB", Context.MODE_PRIVATE, null);
        db.execSQL("drop table if exists TableTennis;");
        db.execSQL("create table TableTennis (ClubName text, Location text);");

        String[] nonPrivateClubNames = {"Impact Table Tennis Club","Triangle Table Tennis","Cape Fear Table Tennis Club","Charlotte Table Tennis Club","New York Indoor Sports Club", "Wang Chen Table Tennis Club","Westchester Table Tennis Center","Princeton Pong", "Lily Yip Table Tennis Center","New Jersey Table Tennis Center","After School Learning Tree Table Tennis Center","Berkeley Table Tennis Club", "Grace Lin Table Tennis Center","ICC Table Tennis Center","Pong Planet","Swan Warriors Table Tennis Center","Sacramento Table Tennis Club", "Newport News Table Tennis Club","Lexington Table Tennis Club","Greenville Table Tennis Club","Greater Columbia Table Tennis Club", "Greater North Augusta Table Tennis Society"};
        clubNames = nonPrivateClubNames;

        String[] nonPrivateLocationNames = {"Apex, NC","Morrisville, NC","Fayetteville, NC","Charlotte, NC","College Point, NY","New York, NY","Pleasantville, NY","Princeton Junction, NJ","Dunellen, NJ","Westfield, NJ","San Diego, CA","Berkeley, CA","South El Monte, CA","Milpitas, CA","San Carlos, CA","Sunnyvale, CA","Sacramento, CA","Newport News, VA","Lexington, KY","Taylors, SC","Columbia, SC","North Augusta, SC"};
        locationNames = nonPrivateLocationNames;

//        for(int i=0;i<22;i++){
//            ContentValues values = new ContentValues();
//            values.put("ClubName", clubNames[i]);
//            values.put("Location", locationNames[i]);
//            db.insert("TableTennis", null, values);
//        }
        for(int i=0;i<22;i++){
            ContentValues values = new ContentValues();
            values.put("ClubName", clubNames[i]);
            String locationName = locationNames[i];
            locationName = locationName.substring(0, locationName.length()-4);
            values.put("Location", locationName);
            db.insert("TableTennis", null, values);
        }
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Toast.makeText(getApplicationContext(),"You gotta install GP Services to use this properly",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop getting location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            findClosestClub();
        }

        startLocationUpdates();
    }

    private void findClosestClub(){
        double[] unsortedDistanceResults = new double[22];
        String[] nameResults = new String[22];
        double smallestDistance, secondSmallestDistance, thirdSmallestDistance;
        String closestCityName, secondClosestCityName, thirdClosestCityName;

        for(int i=0;i<22;i++){
            String[] latestResults = compareDeviceLocationToAnother(locationNames[i]);
            unsortedDistanceResults[i] = Double.parseDouble(latestResults[0]);
            nameResults[i] = latestResults[1];
        }

        double[] sortedDistanceResults = unsortedDistanceResults.clone();
        Arrays.sort(sortedDistanceResults);
        smallestDistance = sortedDistanceResults[0];
        secondSmallestDistance = sortedDistanceResults[1];
        thirdSmallestDistance = sortedDistanceResults[2];

        int closestCityIndex = findIndexOfValueInArray(unsortedDistanceResults, smallestDistance);
        int secondClosestCityIndex = findIndexOfValueInArray(unsortedDistanceResults, secondSmallestDistance);
        int thirdClosestCityIndex = findIndexOfValueInArray(unsortedDistanceResults, thirdSmallestDistance);

        closestCityName = nameResults[closestCityIndex];
        secondClosestCityName = nameResults[secondClosestCityIndex];
        thirdClosestCityName = nameResults[thirdClosestCityIndex];

        //newly added 4/22
        Cursor c1 = db.rawQuery("SELECT ClubName FROM TableTennis WHERE Location='"+closestCityName+"';", null);
        c1.moveToFirst();
        String closestClubName = c1.getString(0);
        Log.v("my_tag772","closest: "+closestClubName);

        Cursor c2 = db.rawQuery("SELECT ClubName FROM TableTennis WHERE Location='"+secondClosestCityName+"';", null);
        c2.moveToFirst();
        String secondClosestClubName = c2.getString(0);
        Log.v("my_tag772","2nd closest: "+secondClosestClubName);

        Cursor c3 = db.rawQuery("SELECT ClubName FROM TableTennis WHERE Location='"+thirdClosestCityName+"';", null);
        c3.moveToFirst();
        String thirdClosestClubName = c3.getString(0);
        Log.v("my_tag772","3rd closest: "+thirdClosestClubName);
        //newly added 4/22



        TextView currentCity = findViewById(R.id.currentCity);
        currentCity.setText("Your Location: " + getMyCityName());

        TextView closestClub = findViewById(R.id.closestClub);
        closestClub.setText(closestClubName);

        int smallestDistanceInMiles = metersToMiles(smallestDistance);
        TextView closestClubDistance = findViewById(R.id.closestClubDistance);
        closestClubDistance.setText(Integer.toString(smallestDistanceInMiles) + " mi");

        TextView SecondClosestClub = findViewById(R.id.SecondClosestClub);
        SecondClosestClub.setText(secondClosestClubName);

        int secondDistanceInMiles = metersToMiles(secondSmallestDistance);
        TextView SecondClosestClubDistance = findViewById(R.id.SecondClosestClubDistance);
        SecondClosestClubDistance.setText(Integer.toString(secondDistanceInMiles) + " mi");

        TextView ThirdClosestClub = findViewById(R.id.ThirdClosestClub);
        ThirdClosestClub.setText(thirdClosestClubName);

        int thirdDistanceInMiles = metersToMiles(thirdSmallestDistance);
        TextView ThirdClosestClubDistance = findViewById(R.id.ThirdClosestClubDistance);
        ThirdClosestClubDistance.setText(Integer.toString(thirdDistanceInMiles) + " mi");


        //my added textviews
        TextView location1 = findViewById(R.id.location1);
        location1.setText(closestCityName);
        TextView location2 = findViewById(R.id.location2);
        location2.setText(secondClosestCityName);
        TextView location3 = findViewById(R.id.location3);
        location3.setText(thirdClosestCityName);

        //add the animation
        while(location3.getText().equals("Distance")) {
            final ImageView buffer = (ImageView) findViewById(R.id.bufferanimation);
            buffer.setBackgroundResource(R.drawable.bufferanimation);
            ((AnimationDrawable) buffer.getBackground()).start();
        }

    }

    private int metersToMiles(double distance){
        double km = distance/1000.0;
        double miles = km/1.609;
        return (int) Math.round(miles);
    }

    private int findIndexOfValueInArray(double[] myArray, double value){
        int length = myArray.length;
        for(int i=0;i<length;i++){
            if(myArray[i] == value){
                return i;
            }
        }

        return -1;
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You gotta turn on permissions to show location.", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(FindClubsActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }
    }

    private String getMyCityName(){
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String cityName = null;

        try{
            List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch(java.io.IOException e){
            e.printStackTrace();
        }

        return cityName;
    }

    //returns an array where the 0th index is the distance betweeen the device and
    //the other specified city in meters. The 1st index is the name of the other city.
    private String[] compareDeviceLocationToAnother(String otherLocationName){
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double distanceBetween;

        try{
            Geocoder gcd2 = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = gcd2.getFromLocationName(otherLocationName,3);
            String locality = "";
            if (addresses.size() > 0) {
                locality = addresses.get(0).getLocality();
            }

            Address address0 = addresses.get(0);
            double rLat = address0.getLatitude();
            double rLong = address0.getLongitude();
            Log.v("my_loc","Locality: "+locality+", lat: "+rLat+", long: "+rLong);


            float[] results = new  float[3];
            Location.distanceBetween(lat, lng,
                    rLat, rLong, results);

            Log.v("my_loc","distance between: "+results[0]);
            distanceBetween = results[0];
            String[] resultsForReturn = {Double.toString(distanceBetween),locality};
            return resultsForReturn;
        } catch(java.io.IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public void goBack(View v) {
        onBackPressed();
    }
}

