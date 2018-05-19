package com.sarts.shivi.sos.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sarts.shivi.sos.UI.HomeActivity;
import com.sarts.shivi.sos.storage.Storage;

import java.util.List;

public class AlertService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private String activity = "";
    private Storage storage;

    public static final String broadcast_update_map = "com.sarts.shivi.sos.updatemap";

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public AlertService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*if (intent != null) {
            if (intent.getStringExtra("Activity") != null) {
                activity = intent.getStringExtra("Activity");
            }
        }*/

        return START_STICKY;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendlocationbroadcast();
        }
    };

    private void sendlocationbroadcast() {

        Intent intent = new Intent(broadcast_update_map);
        intent.putExtra("location",getLocation());

        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        Log.i("location","location sent");
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("Service","started");
        storage = new Storage(getApplicationContext());
        locationManager =(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location loc) {
                if (isBetterLocation(loc, location)) {
                    location = loc;
                }
                String emer = storage.getemergencystate();
                if (emer != null){
                    if (emer.equals("NO")) {

                        sendlocationbroadcast();
                    }
                }
                savelocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0,locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);

            savelocation();
            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(broadcastReceiver,new IntentFilter(HomeActivity.mapready));
        }
        catch (SecurityException e){
            Log.i("Security Exception",e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public Location getLocation(){

        try {
            //Log.i("location",String.valueOf(location));
            if (isBetterLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),location)){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isBetterLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER),location)){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            return location;
        }
        catch (SecurityException e){
            Log.i("Security Exception",e.getMessage());
            return null;
        }
    }

    public void findNear(final Location location){
        Log.i("Sending Request","7");
        if(location!=null){
            Log.i("Sending Request","8");
            ParseQuery<ParseObject> query= ParseQuery.getQuery("Request");
            final ParseGeoPoint parseGeoPoint=new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            query.whereNear("location",parseGeoPoint);
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){
                        for (ParseObject object : objects){
                            Log.i("Sending Request","10");
                            object.put("victimLocation",location);
                        }
                    }
                }
            });
        }
    }

    public LocationListener getLocationListener() {
        return locationListener;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }





    /** Determines whether one Location reading is better than the current Location fix
     * @param loc  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location loc, Location currentBestLocation) {
        //Log.i("is better location",String.valueOf(currentBestLocation));
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        if (loc != null) {
            // Check whether the new location fix is newer or older
            long timeDelta = loc.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (loc.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(loc.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public void savelocation(){

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("userid", parseUser.getObjectId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects != null) {
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.deleteInBackground();
                            }
                        }
                    }
                }
            });

            ParseObject request = new ParseObject("Request");
            request.put("userid", parseUser.getObjectId());
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(getLocation().getLatitude(), getLocation().getLongitude());
            request.put("location", parseGeoPoint);
            //request.put("Level",ratingBar.getNumStars());
            request.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i("Status", "Location updated");
                    } else {
                        Log.i("Status", e.getMessage());
                    }
                }
            });
        }
    }
}
