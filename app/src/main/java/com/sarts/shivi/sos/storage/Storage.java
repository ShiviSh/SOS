package com.sarts.shivi.sos.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

/**
 * Created by Shivi on 11-01-2018.
 */

public class Storage {

    SharedPreferences sharedPreferences;

    public Storage(Context context){
        sharedPreferences = context.getSharedPreferences("com.sarts.shivi.sos",Context.MODE_PRIVATE);
    }

    public void setemergencystate(String emergency){
        sharedPreferences.edit().putString("emergency",emergency).apply();
    }

    public String getemergencystate(){
        return sharedPreferences.getString("emergency",null);
    }

    public void setsessiontoken(String token){
        sharedPreferences.edit().putString("session token",token).apply();
    }

    public String getsessiontoken(){
        return sharedPreferences.getString("session token",null);
    }
}
