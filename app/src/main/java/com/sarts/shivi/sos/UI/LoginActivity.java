package com.sarts.shivi.sos.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.sarts.shivi.sos.R;
import com.sarts.shivi.sos.service.AlertService;
import com.sarts.shivi.sos.storage.Storage;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Storage storage;


    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private EditText mPhoneView;
    private EditText signupname;
    private EditText signupmobileno;
    private EditText signuppassword;

    private Button signupbutton;

    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        storage = new Storage(getApplicationContext());

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintlayout);
        signupmobileno = (EditText)findViewById(R.id.mobilenoinput);
        signupname = (EditText)findViewById(R.id.nameinput);
        signuppassword = (EditText)findViewById(R.id.passwordinput);

        mPhoneView = (EditText) findViewById(R.id.phone);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.emailinput);
        populateAutoComplete();

        signupbutton = (Button) findViewById(R.id.signupbutton);

        mPasswordView = (EditText) findViewById(R.id.password);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        if (savedInstanceState != null){
            mPasswordView = savedInstanceState.getParcelable("password");
            mEmailView = savedInstanceState.getParcelable("email");
            signupname = savedInstanceState.getParcelable("signupname");
            signuppassword = savedInstanceState.getParcelable("signuppassword");
            signupmobileno = savedInstanceState.getParcelable("signmobileno");
            mPhoneView = savedInstanceState.getParcelable("phone");
        }

        signuppassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (constraintLayout.getVisibility() == View.VISIBLE){
                        signUp(constraintLayout);
                    }
                    return true;
                }
                return false;
            }
        });


        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (mLoginFormView.getVisibility() == View.VISIBLE){
                        attemptLogin();
                    }
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneNoValid(phone)){
            mPhoneView.setError("Mobile No is Invalid");
            focusView = mPhoneView;
            cancel = true;
        }


        /*else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(phone, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPhoneNoValid(String phone){

        return phone.length() == 10;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public void OrRegister(View view) {


        mLoginFormView.setVisibility(View.GONE);
        constraintLayout.setVisibility(View.VISIBLE);



    }


    @Override
    public void onBackPressed() {
        if (constraintLayout.getVisibility() == View.VISIBLE){
            constraintLayout.setVisibility(View.GONE);
            mEmailView.getText().clear();
            signupmobileno.getText().clear();
            signuppassword.getText().clear();
            signupname.getText().clear();
            mPhoneView.getText().clear();
            mPasswordView.getText().clear();
            mLoginFormView.setVisibility(View.VISIBLE);
        }
        else {
            super.onBackPressed();
        }

    }

    public void signUp(View view) {

        signuppassword.setError(null);
        signupmobileno.setError(null);
        mEmailView.setError(null);
        signupname.setError(null);
        Log.i("Signup","Called");

        boolean cancel = false;
        final String name = signupname.getText().toString();
        String mobileno = signupmobileno.getText().toString();
        final String password = signuppassword.getText().toString();
        String email = mEmailView.getText().toString();

        if (TextUtils.isEmpty(name)){
            signupname.setError("Enter your Name");
            signupname.requestFocus();
            cancel = true;
        }

        if (TextUtils.isEmpty(mobileno)) {
            signupmobileno.setError(getString(R.string.error_field_required));
            signupmobileno.requestFocus();
            cancel = true;
        } else if (!isPhoneNoValid(mobileno)){
            signupmobileno.setError("Mobile No is Invalid");
            signupmobileno.requestFocus();
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();
            cancel = true;
        } else if (!isEmailValid(email)){
            mEmailView.setError("Email is Invalid");
            mEmailView.requestFocus();
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            signuppassword.setError(getString(R.string.error_invalid_password));
            signuppassword.requestFocus();
            cancel = true;
        }
        if (!cancel){
            ParseUser parseUser = new ParseUser();
            parseUser.setEmail(email);
            parseUser.setUsername(mobileno);
            parseUser.setPassword(password);
            parseUser.put("name",name);
            parseUser.put("phone",Long.valueOf(mobileno));

            //ParseUser.getCurrentUser().getSessionToken();

            parseUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null ){
                        Log.i("SignUp","Successful");
                        final ParseUser parseUser = ParseUser.getCurrentUser();
                        storage.setsessiontoken(ParseUser.getCurrentUser().getSessionToken());
                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.put("userid",parseUser.getObjectId());
                        installation.saveInBackground();
                        ParseACL defaultACL = new ParseACL();
                        defaultACL.setPublicReadAccess(true);
                        defaultACL.setPublicWriteAccess(true);
                        parseUser.setACL(defaultACL);

                        String email = parseUser.getEmail();
                        firebaseAuth = FirebaseAuth.getInstance();

                        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){

                                    Log.i("Firebase user","created");
                                    databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                                    DatabaseReference current=databaseReference.child(firebaseAuth.getCurrentUser().getUid());
                                    current.child("Name").setValue(name);
                                    //databaseReference.setValue(databaseReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(child("Name"),name));
                                    Intent intent = new Intent(LoginActivity.this,AlertService.class);
                                    startService(intent);
                                }
                            }
                        });
                        /*ParseObject parseObject = new ParseObject("Request");
                        parseObject.put("userid",parseUser.getObjectId());
                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                if (e == null){
                                    Log.i("done","kvkdjvkdvd");
                                }
                                else {
                                    Log.i("error",e.getMessage());
                                }
                            }
                        });*/
                        Intent intent = new Intent(LoginActivity.this,AlertService.class);
                        startService(intent);
                        startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                    }
                    else {
                        Log.i("SignUp","Failed : " + e.getMessage());
                    }
                }
            });
        }

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPassword;

        UserLoginTask(String phone, String password) {
            mPhone = phone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            ParseUser.logInInBackground(mPhone, mPassword, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {

                    if (user != null){
                        Log.i("Login","Successful");
                        ParseUser parseUser = ParseUser.getCurrentUser();
                        storage.setsessiontoken(ParseUser.getCurrentUser().getSessionToken());
                        String email = parseUser.getEmail();
                        firebaseAuth = FirebaseAuth.getInstance();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
                        firebaseAuth.signInWithEmailAndPassword(email,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                                }
                            }
                        });

                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.put("userid",parseUser.getObjectId());
                        installation.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    Log.i("hhhhhdone","kvkdjvkdvd");
                                }
                                else {
                                    Log.i("error instal",e.getMessage());
                                }
                            }
                        });
                        /*ParseObject parseObject = new ParseObject("Request");

                        parseObject.put("userid",parseUser.getObjectId());
                        parseObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                if (e == null){
                                    Log.i("done","kvkdjvkdvd");
                                }
                                else {
                                    Log.i("error",e.getMessage());
                                }
                            }
                        });*/
                        Intent intent = new Intent(LoginActivity.this,AlertService.class);
                        startService(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("email",mEmailView.onSaveInstanceState());
        outState.putParcelable("signuppassword",signuppassword.onSaveInstanceState());
        outState.putParcelable("signupname",signupname.onSaveInstanceState());
        outState.putParcelable("signmobileno",signupmobileno.onSaveInstanceState());
        outState.putParcelable("phone",mPhoneView.onSaveInstanceState());
        outState.putParcelable("password",mPasswordView.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }
}

