package com.sarts.shivi.sos.UI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sarts.shivi.sos.R;
import com.sarts.shivi.sos.UI.home.SecPageAdapter;
import com.sarts.shivi.sos.UI.home.dummy.Message;
import com.sarts.shivi.sos.service.AlertService;
import com.sarts.shivi.sos.storage.Storage;

import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SecPageAdapter secPageAdapter;
    private FirebaseAuth mAuth;
    private RecyclerView mMessageList;
    public static Location lastKnownLocation;
    private DatabaseReference mDatabase;

    public static final String mapready = "com.sarts.shivi.sos.mapready";

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastKnownLocation = (Location) intent.getExtras().getParcelable("location");
            Log.i("Send alert",lastKnownLocation.toString());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.



        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        secPageAdapter = new SecPageAdapter(getSupportFragmentManager());

        setupViewPager(mViewPager);

        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(broadcastReceiver,new IntentFilter(AlertService.broadcast_update_map));
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent(HomeActivity.mapready));


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mMessageList = new  MainFragment().getmMessageList();
        mDatabase = new MainFragment().getmDatabase();


        mAuth = FirebaseAuth.getInstance();


        /*mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                }
            }
        };*/



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_exit){
            //stopService(new Intent(getApplicationContext(),AlertService.class));
            Process.killProcess(Process.myPid());
            return true;
        }


        else if ( id == R.id.action_logout)
        {
            if (ParseUser.getCurrentUser() != null) {
                final ParseUser parseUser = ParseUser.getCurrentUser();
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getApplicationContext(), "Successfuly logged out", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Session");
                            parseQuery.whereEqualTo("user",parseUser);
                            parseQuery.whereEqualTo("installationId", ParseInstallation.getCurrentInstallation().getInstallationId());
                            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    for (ParseObject object: objects){
                                        object.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null){
                                                    Log.i("Logout", "Session removed");
                                                }
                                                else {
                                                    Log.i("Logout", "Session remove failed");
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                            finish();

                        } else {
                            Log.i("Logout error", e.getMessage());
                        }
                    }
                });
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    private void setupViewPager(ViewPager viewPager){
        SecPageAdapter adapter = new SecPageAdapter(getSupportFragmentManager());



        adapter.addFragment(new HomeActivity.ProfileFragment(),"Profile");
        adapter.addFragment(new HomeActivity.MainFragment(),"Network");
        adapter.addFragment(new HomeActivity.MapFragment(),"Map");
        viewPager.setAdapter(adapter);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public MessageViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setContent(String content){
            TextView message_content =mView.findViewById(R.id.messageText);
            message_content.setText(content);
        }

        public  void setUsername(String username){
            Log.i("Info","Redirecting 31 ");
            TextView username_content=mView.findViewById(R.id.usernameText);
            username_content.setText(username);
        }
    }



    public static class MainFragment extends android.support.v4.app.Fragment {

        HomeActivity homeActivity = new HomeActivity();
        private DatabaseReference mDatabase;
        private FirebaseUser mCurrentUser;
        private FirebaseAuth mAuth;
        private DatabaseReference mDatabaseUsers;
        private FirebaseAuth.AuthStateListener mAuthListener;
        private Button sendButton;

        public RecyclerView getmMessageList() {
            return mMessageList;
        }

        private RecyclerView mMessageList;
        private EditText editMessage;

        @Override
        public void onStart() {
            super.onStart();

            mAuth.addAuthStateListener(mAuthListener);
            FirebaseRecyclerAdapter<Message,MessageViewHolder> FBRA= new FirebaseRecyclerAdapter<Message,MessageViewHolder>(
                    Message.class,
                    R.layout.newmsglayout,
                    MessageViewHolder.class,
                    mDatabase
            ){
                @Override
                protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                    viewHolder.setContent(model.getContent());
                    viewHolder.setUsername(model.getUsername());
                    Log.i("Info","Redirecting 30 ");
                }
            };
            mMessageList.setAdapter(FBRA);
        }

        public MainFragment() {
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.tab_main, container, false);
            //editMessage =findViewById(R.id.text);

            Log.i("main view","created");
            mAuth = FirebaseAuth.getInstance();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");
            sendButton = view.findViewById(R.id.send);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendButton(v);
                }
            });

            mMessageList = view.findViewById(R.id.messageRec);
            editMessage = view.findViewById(R.id.text);
            mMessageList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setStackFromEnd(true);
            mMessageList.setLayoutManager(linearLayoutManager);



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(getContext(),LoginActivity.class));
                }
            }
        };
            return view;
        }


        public DatabaseReference getmDatabase(){
            return mDatabase;
        }


        public void sendButton(View view) {
            Log.i("Info", "Redirecting 33 ");
            mCurrentUser = mAuth.getCurrentUser();
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
            final String messageValue = editMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageValue)) {
                Log.i("Info", "Redirecting 32 ");
                final DatabaseReference newPost = mDatabase.push();
                mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newPost.child("content").setValue(messageValue);
                        newPost.child("name").setValue(ParseUser.getCurrentUser().get("name"));
                        Log.i("Info", "Redirecting 29 ");
                        newPost.child("username").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mMessageList.scrollToPosition(mMessageList.getAdapter().getItemCount() - 1);
            }
        }

    }


    public static class MapFragment extends Fragment {

        private GoogleMap mMap;
        HashMap<String,Marker> hashMapMarker = new HashMap<>();


        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                updateMap((Location) intent.getExtras().getParcelable("location"), "Your Location");
            }
        };

        public static final String mapready = "com.sarts.shivi.sos.mapready";

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.tab_map, container, false);

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(AlertService.broadcast_update_map));

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.home_map_fragment);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;


                    if (getActivity().getIntent() != null) {
                        String show = getActivity().getIntent().getStringExtra("show");
                        if (show != null) {
                            if (show.equals("marker")) {
                                ParseUser parseUser = ParseUser.getCurrentUser();

                                Location location = new Location("Parse");

                                ParseGeoPoint parseGeoPoint = (ParseGeoPoint) parseUser.get("victimlocation");
                                double latitiude = parseGeoPoint.getLatitude();
                                double longitude = parseGeoPoint.getLongitude();
                                location.setLongitude(longitude);
                                location.setLatitude(latitiude);

                                Log.i("geo","failed " + location+mMap);
                       /* ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
                        query.whereEqualTo("username",parseUser.getUsername());
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if ( e==null && objects.size()>0 ){

                                    for(int i= 0; i < objects.size(); i++) {
                                        // Now get Latitude and Longitude of the object
                                        double queryLatitude = objects.get(i).getParseGeoPoint("Location").getLatitude();
                                        double queryLongitude = objects.get(i).getParseGeoPoint("Location").getLongitude();
                                        location.setLatitude(queryLatitude);
                                        location.setLongitude(queryLongitude);
                                    }
                                }
                            }
                        });*/

                                updateMap(location, "Victim");
                            }
                        }
                    }

                    Intent intent = new Intent(mapready);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
            });


            return view;
        }




        public void updateMap(Location location, String description) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            float color = BitmapDescriptorFactory.HUE_RED;
            if (description.equals("Victim")) {
                color = BitmapDescriptorFactory.HUE_BLUE;

            } else if (description.equals("Your Location")) {

                if (hashMapMarker.get(description) != null) {
                    Marker marker = hashMapMarker.get(description);
                    marker.remove();
                    hashMapMarker.remove(description);
                }

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));

            Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(description).icon(BitmapDescriptorFactory.defaultMarker(color)));
            hashMapMarker.put(description,marker);

        }
    }





    public static class ProfileFragment extends android.support.v4.app.Fragment {


        TextView name;
        Button button;
        Location loc;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            View view=inflater.inflate(R.layout.tab_profile,container,false);
            name = view.findViewById(R.id.textView4);
            button = view.findViewById(R.id.floatingActionButton);
            Storage storage = new Storage(getContext());
            if (storage.getemergencystate() != null){
                setestate(storage.getemergencystate());

            }
            name.setText(ParseUser.getCurrentUser().getString("name"));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendalert();
                }
            });
            return view;
        }

        public void setLocation(Location location){
            loc = new Location("parse");
            loc = location;
            Log.i("alert",loc.toString());
        }

        public void setestate(String s){

            if (s.equals("NO")){
                button.setText("Send SOS");
            }
            else if (s.equals("YES")){
                button.setText("Cancel SOS");
            }
        }
        public void sendalert(){
            loc = HomeActivity.lastKnownLocation;

            Button requestButton = button;

            Log.i("Sending Request", "1");
            ParseUser user = ParseUser.getCurrentUser();

            if (user.getBoolean("isRequested")) {
                Log.i("Sending Request", "2");
                user.put("isRequested",false);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null){
                            Log.i("request","UPDATE SUCCESSFUL");
                        }
                        else {
                            Log.i("request","UPDATE Failed");
                        }
                    }
                });
                requestButton.setText("Send SOS");
                new Storage(getContext()).setemergencystate("NO");

                ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
                parseQuery.whereEqualTo("victimid",user.getObjectId());
                parseQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if (objects != null){
                            if (objects.size() > 0){
                                for (ParseUser parseObject: objects){
                                    //ParseUser parseUser1 = (ParseUser) parseObject;
                                    parseObject.put("victimid","");
                                    parseObject.put("victimlocation",new ParseGeoPoint());
                                    parseObject.put("nearby",false);
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null){
                                                Log.i("victim","reset");
                                            }
                                            else {
                                                Log.i("victim","reset failed "  + e.getMessage());
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });


            } else {
                Log.i("Sending Request", "3");
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                if (true) {
                    ParseUser parseUser = ParseUser.getCurrentUser();


                    parseUser.put("isRequested",true);
                    parseUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                Log.i("request","UPDATE SUCCESSFUL");
                            }
                            else {
                                Log.i("request","UPDATE Failed");
                            }
                        }
                    });
                    Log.i("aaaaaaa",loc.toString());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(loc.getLatitude(),loc.getLongitude());
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("victimid", parseUser.getObjectId());
                    params.put("victimlocation", parseGeoPoint);
                    ParseCloud.callFunctionInBackground("updateUser", params, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, ParseException e) {

                            if (e == null){
                                Log.i("messege","sent");
                                if (object != null){
                                    Log.i("object",object.toString());
                                }
                                else {
                                    Log.i("object","null");
                                }
                            }
                            else {
                                Log.i("messege","Failed :" + e.getMessage());
                            }
                        }
                    });

                    requestButton.setText("Cancel SOS");

                /*params = new HashMap<String, Object>();
                params.put("appName","SOS");
                params.put("message","Hikhkk");
                ParseCloud.callFunctionInBackground("sendPushToUser", params, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object object, ParseException e) {

                       if (e == null){
                            Log.i("messege","sent");
                            if (object != null){
                                Log.i("object",object.toString());
                            }
                            else {
                                Log.i("object","null");
                            }
                        }
                        else {
                            Log.i("messege","Failed :" + e.getMessage());
                        }
                    }
                });*/

                } else {
                    Toast.makeText(getContext(), "Could not find location.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }




}
