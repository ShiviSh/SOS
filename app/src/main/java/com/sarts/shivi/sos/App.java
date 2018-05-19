package com.sarts.shivi.sos;

import android.app.Application;
import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.util.HashMap;


/**
 * Created by Shivi on 09-01-2018.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //if (Parse.Buddy.skipApplicationOnCreate(this)) return;

        Parse.initialize(this);

        //ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        //installation.put("user",ParseUser.getCurrentUser());
        installation.saveInBackground();
        Log.i("install",ParseInstallation.getCurrentInstallation().getInstallationId());


        /*ParseObject object = new ParseObject("ExampleObject");
        object.put("myNumber", "5555");
        object.put("myString", "SS");

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    Log.i("Parse Result", "Successful!");
                } else {
                    Log.i("Parse Result", "Failed" + ex.toString());
                }
            }
        });
        */


        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
