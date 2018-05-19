package com.sarts.shivi.sos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.sarts.shivi.sos.UI.HomeActivity;
import com.sarts.shivi.sos.UI.LoginActivity;
import com.sarts.shivi.sos.service.AlertService;
import com.sarts.shivi.sos.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private boolean granted = true;
    private int storage_perm;
    private int networkstate_perm;
    private int internet_perm;
    private int location_perm;
    private int accounts_perm;
    private int contacts_perm;
    private List<String> permissions_needed;

    private Storage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Parse.Buddy.initialize();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());



        storage_perm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        internet_perm = ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET);
        location_perm = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        contacts_perm = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS);
        accounts_perm = ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS);
        networkstate_perm = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE);
        permissions_needed = new ArrayList<>();

        if (storage_perm != PackageManager.PERMISSION_GRANTED){
            permissions_needed.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            granted = false;
        }
        if (internet_perm != PackageManager.PERMISSION_GRANTED){
            permissions_needed.add(Manifest.permission.READ_PHONE_STATE);
            granted = false;
        }
        if (accounts_perm != PackageManager.PERMISSION_GRANTED){
            permissions_needed.add(Manifest.permission.GET_ACCOUNTS);
            granted = false;
        }
        if (location_perm != PackageManager.PERMISSION_GRANTED){
            permissions_needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
            granted = false;
        }
        if (networkstate_perm != PackageManager.PERMISSION_GRANTED){
            permissions_needed.add(Manifest.permission.ACCESS_NETWORK_STATE);
            granted = false;
        }
        if (contacts_perm != PackageManager.PERMISSION_GRANTED){
            permissions_needed.add(Manifest.permission.READ_CONTACTS);
            granted = false;
        }
        if (!permissions_needed.isEmpty())
        {
            ActivityCompat.requestPermissions(this, permissions_needed.toArray(new String[permissions_needed.size()]),1);
        }
        else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    storage = new Storage(getApplicationContext());
                    //Log.i("Handler","runing : " + ParseUser.getCurrentUser().getUsername());
                    if (ParseUser.getCurrentUser() == null) {
                        Log.i("main activity","no current user");
                        if (storage.getsessiontoken() == null) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        } else {
                            ParseUser.becomeInBackground(storage.getsessiontoken(), new LogInCallback() {
                                @Override
                                public void done(ParseUser user, ParseException e) {
                                    Log.i("main activity","trying to sign in current user");
                                    if (user != null && e == null) {
                                        Log.i("Login", "Successful");
                                        storage.setsessiontoken(ParseUser.getCurrentUser().getSessionToken());
                                        if (FirebaseAuth.getInstance().getCurrentUser() == null){
                                            ParseUser parseUser = ParseUser.getCurrentUser();
                                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                            firebaseAuth.signInWithEmailAndPassword(parseUser.getEmail(),parseUser.getString("password")).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()){
                                                        Log.i("Firebase","Logged back in");
                                                    }
                                                    else {
                                                        Log.i("Firebase","log back failed");
                                                    }
                                                }
                                            });
                                        }
                                        Intent intent = new Intent(MainActivity.this, AlertService.class);
                                        startService(intent);
                                        startActivity(new Intent(MainActivity.this,HomeActivity.class));
                                    } else {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    }
                                }
                            });
                        }
                    }
                    else {
                        Intent intent = new Intent(MainActivity.this, AlertService.class);
                        startService(intent);
                        startActivity(new Intent(MainActivity.this,HomeActivity.class));

                    }
                }
            },2000);




        }




    }


    @Override
    protected void onNewIntent(Intent intent) {


        if (intent != null){
            String source = intent.getStringExtra("source");
            if (source != null) {
                if (source.equals("notification")) {
                    Intent mapintent = new Intent(MainActivity.this, HomeActivity.class);
                    mapintent.putExtra("show","marker");
                    startActivity(mapintent);
                }
            }
        }

        super.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for ( int grant : grantResults){
                        if (grant != PackageManager.PERMISSION_GRANTED){
                            Log.i("pemisssion result", "denied, some permissions are denied" );
                            exitting();
                            break;
                        }
                    }
                    granted = true;
                    startActivity(new Intent(this,LoginActivity.class));
                }
                else {
                    Log.i("pemisssion result", "denied, none granted" );
                    exitting();
                }
                break;
            default:
                Log.i("pemisssion result", "denied" );
                exitting();
        }
    }

    private void exitting() {

        Toast.makeText(getApplicationContext(),"Exitting App,Permission Denied",Toast.LENGTH_LONG).show();
        Process.killProcess(Process.myPid());
    }
}
