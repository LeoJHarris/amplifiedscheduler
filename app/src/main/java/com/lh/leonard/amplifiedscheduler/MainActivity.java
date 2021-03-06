package com.lh.leonard.amplifiedscheduler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.kobakei.ratethisapp.RateThisApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import se.simbio.encryption.Encryption;

public class MainActivity extends Activity {

    private String username, password, usernameDecrypted, passwordDecrypted;

    private EditText editTextUsername, editTextPassword;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    Button facebookButton;
    ProgressDialog ringProgressDialog;
    Boolean loggedOutPersons = false;
    Encryption encryption;
    Bundle extras;

    Boolean useBackButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Backendless.initApp(this, Defaults.APPLICATION_ID, Defaults.SECRET_KEY, Defaults.VERSION);

        // Custom criteria: 3 days and 5 launches
        RateThisApp.Config config = new RateThisApp.Config(20, 15);
        // Custom title and message
        config.setTitle(R.string.rta_dialog_title);
        config.setMessage(R.string.rta_dialog_message);
        RateThisApp.init(config);

        Backendless.Data.mapTableToClass("Person", Person.class);
        Backendless.Data.mapTableToClass("Slot", Slot.class);

        extras = getIntent().getExtras();

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        if (extras != null) {
            if (extras.getString("loggedoutperson") != null && (loginPreferences.getBoolean("saveLogin", false))) {
                Toast.makeText(getApplicationContext(), "Just fetching your sign in credentials", Toast.LENGTH_LONG).show();
                useBackButton = true;

            } else if (extras.getString("nameRegistered") != null) {
                loginPrefsEditor.clear();
                loginPrefsEditor.apply();
                String NameArray = extras.getString("nameRegistered");
                assert NameArray != null;
                String[] NameSplit = NameArray.split(",");

                Toast.makeText(getApplication(),
                        "Successfully registered account for " + NameSplit[0] + " " + NameSplit[1],
                        Toast.LENGTH_LONG).show();
            }
        }

        encryption = Encryption.getDefault("Key", "Salt", new byte[16]);  // 16

        new Change().execute();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public class Validator {

        private boolean isPasswordValid(CharSequence password) {
            // At least Length 5
            return password.toString().length() > 4;
        }

        private boolean isValidEmail(CharSequence target) {
            return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public void onBackPressed() {

        if (loggedOutPersons) {
            this.finishAffinity();
        } else if (useBackButton) {
            Toast.makeText(getApplicationContext(), "Almost ready, please wait", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private class Change extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {


            saveLogin = loginPreferences.getBoolean("saveLogin", false);
            if (saveLogin) {
                passwordDecrypted = encryption.decryptOrNull(loginPreferences.getString("password", ""));
                usernameDecrypted = loginPreferences.getString("username", "");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            MainActivity.this.setContentView(R.layout.activity_main);

            facebookButton = (Button) findViewById(R.id.FBlogin_button);

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Map<String, String> facebookFieldMappings = new HashMap<String, String>() {{
                        put("email", "email");
                        put("gender", "gender");
                        put("last_name", "lname");
                        put("first_name", "fname");
                        put("picture", "picture");
                    }};

                    List<String> permissions = new ArrayList<>();
                    permissions.add("public_profile");
                    permissions.add("email");

                    try {
                        Backendless.UserService.loginWithFacebook(MainActivity.this, null, facebookFieldMappings, permissions, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {

                                Person p = new Person();
                                p.setGender((String) response.getProperty("gender"));
                                p.setLname((String) response.getProperty("lname"));
                                p.setFname((String) response.getProperty("fname"));
                                p.setFullname(response.getProperty("fname") + " " +
                                        response.getProperty("lname"));
                                p.setEmail((String) response.getProperty("email"));

                                try {
                                    String url;
                                    if(response.getProperty("picture") !=null) {
                                        JSONObject jsonAdd = new JSONObject((String) response.getProperty("picture"));
                                        Boolean isSilhouette = jsonAdd.getJSONObject("data").getBoolean("is_silhouette");


                                        if (isSilhouette) {
                                            url = "https://api.backendless.com/e9c78af2-bdc4-c5fa-ff3f-da79004b9200/v1/files/pictures/silhouette.png";
                                        } else {
                                            url = jsonAdd.getJSONObject("data").getString("url");
                                        }
                                        p.setSilhouette(isSilhouette);
                                    }
                                    else{
                                        url = "https://api.backendless.com/e9c78af2-bdc4-c5fa-ff3f-da79004b9200/v1/files/pictures/silhouette.png";
                                    }

                                    p.setPicture(url);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    new Save(p, response).execute().get();
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(getApplicationContext(), fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },false);
                    } catch (Exception e) {
                        Log.e("YOUR_APP_LOG_TAG", "I got an error", e);
                    }
                }
            });

            useBackButton = false;

            if (extras != null) {
                if (extras.getString("loggedoutperson") != null) {
                    String NameArray = extras.getString("loggedoutperson");
                    assert NameArray != null;
                    String[] NameSplit = NameArray.split(",");

                    Toast.makeText(getApplication(),
                            "Successfully logged out " + NameSplit[0] + " " + NameSplit[1],
                            Toast.LENGTH_LONG).show();
                    loggedOutPersons = true;
                } else if (extras.getString("loggedoutpersonError") != null) {
                    String NameArray = extras.getString("loggedoutperson");
                    assert NameArray != null;
                    String[] NameSplit = NameArray.split(",");

                    Toast.makeText(getApplication(),
                            "Error occurred: Logged out " + NameSplit[0] + " " + NameSplit[1],
                            Toast.LENGTH_LONG).show();
                    loggedOutPersons = true;
                }
            }

            editTextUsername = (EditText) findViewById(R.id.emailSignIn);
            editTextPassword = (EditText) findViewById(R.id.passwordSignIn);
            saveLoginCheckBox = (CheckBox) findViewById(R.id.saveLoginCheckBox);

            if (saveLogin) {
                saveLoginCheckBox.setChecked(true);
                editTextPassword.setText(passwordDecrypted);
                editTextUsername.setText(usernameDecrypted);
            }

            Button buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
            AutoResizeTextView buttonForgotPassword = (AutoResizeTextView) findViewById(R.id.buttonForgotPassword);
            AutoResizeTextView buttonRegistration = (AutoResizeTextView) findViewById(R.id.buttonRegistration);
            AutoResizeTextView textViewMadeByMeMain = (AutoResizeTextView) findViewById(R.id.textViewMadeByMeMain);
            ImageView imageViewMainLogo = (ImageView) findViewById(R.id.imageViewMainLogo);
            AutoResizeTextView spacer = (AutoResizeTextView) findViewById(R.id.spacer);

            final Typeface RobotoCondensedLightItalic = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RobotoCondensed-LightItalic.ttf");
            final Typeface RobotoCondensedLight = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RobotoCondensed-Light.ttf");

            spacer.setTypeface(RobotoCondensedLight);
            buttonSignIn.setTypeface(RobotoCondensedLight);
            editTextUsername.setTypeface(RobotoCondensedLight);
            editTextPassword.setTypeface(RobotoCondensedLight);
            buttonForgotPassword.setTypeface(RobotoCondensedLight);
            buttonRegistration.setTypeface(RobotoCondensedLight);
            textViewMadeByMeMain.setTypeface(RobotoCondensedLightItalic);
            saveLoginCheckBox.setTypeface(RobotoCondensedLight);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            //Fame
            if (width == 320 && height == 480) {
                imageViewMainLogo.requestLayout();
                imageViewMainLogo.getLayoutParams().height = 80;
                editTextUsername.setTextSize(15);
                editTextUsername.setPadding(4, 4, 4, 4);
                editTextPassword.setPadding(4, 4, 4, 4);
                editTextPassword.setTextSize(15);
                buttonForgotPassword.setTextSize(15);
                buttonRegistration.setTextSize(15);
                textViewMadeByMeMain.setTextSize(11);
                buttonSignIn.setTextSize(15);
                buttonSignIn.setPadding(0, 0, 0, 0);
                saveLoginCheckBox.setTextSize(15);
            }
            // 2.7" QVGA
            else if (width == 240 && height == 320) {
                imageViewMainLogo.requestLayout();
                imageViewMainLogo.getLayoutParams().height = 40;
                editTextUsername.setTextSize(16);
                imageViewMainLogo.setPadding(0, 10, 0, 0);
                editTextUsername.setPadding(7, 7, 7, 7);
                editTextPassword.setPadding(7, 7, 7, 7);
                buttonSignIn.setPadding(1, 1, 1, 1);
                editTextPassword.setTextSize(16);
                saveLoginCheckBox.setTextSize(16);
                buttonForgotPassword.setTextSize(12);
                buttonRegistration.setTextSize(12);
                textViewMadeByMeMain.setTextSize(10);
                buttonSignIn.setTextSize(16);
            } else if (width == 240 && height == 432) {
                editTextUsername.setTextSize(18);
                imageViewMainLogo.setPadding(0, 10, 0, 0);
                editTextUsername.setPadding(7, 7, 7, 7);
                editTextPassword.setPadding(7, 7, 7, 7);
                buttonSignIn.setPadding(1, 1, 1, 1);
                editTextPassword.setTextSize(18);
                saveLoginCheckBox.setTextSize(18);
                buttonForgotPassword.setTextSize(18);
                buttonRegistration.setTextSize(18);
                textViewMadeByMeMain.setTextSize(15);
                buttonSignIn.setTextSize(18);
            }
            SpannableString forgotPassword = new SpannableString(buttonForgotPassword.getText());
            forgotPassword.setSpan(new UnderlineSpan(), 0, forgotPassword.length(), 0);
            buttonForgotPassword.setText(forgotPassword);

            SpannableString signup = new SpannableString(buttonRegistration.getText());
            signup.setSpan(new UnderlineSpan(), 0, signup.length(), 0);
            buttonRegistration.setText(signup);


            //Should be async, user has gotto main activity and still logged in
            if (Backendless.UserService.CurrentUser() != null) {
                BackendlessUser user = Backendless.UserService.CurrentUser();
                final Person personLoggedOut = (Person) user.getProperty("loggedoutperson");

                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    public void handleResponse(Void response) {
                        if (personLoggedOut != null && personLoggedOut.getFname() != null && personLoggedOut.getFname() != null)
                            Toast.makeText(getApplicationContext(), personLoggedOut.getFname() + "," + personLoggedOut.getFname(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {

                    }
                });
            }

            buttonRegistration.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Intent registerIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                    startActivity(registerIntent);
                }
            });

            buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent recoveryPasswordIntent = new Intent(MainActivity.this, ForgotPasswordReset.class);
                    startActivity(recoveryPasswordIntent);
                }
            });

            buttonSignIn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    EditText emailField = (EditText) findViewById(R.id.emailSignIn);
                    EditText passwordField = (EditText) findViewById(R.id.passwordSignIn);

                    if (new Validator().isValidEmail(emailField.getText())) {
                        if (new Validator().isPasswordValid(passwordField.getText())) {

                            ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Signing in ...", true);
                            ringProgressDialog.setCancelable(true);

                            Backendless.UserService.login(emailField.getText().toString(), passwordField.getText().toString(), new AsyncCallback<BackendlessUser>() {

                                public void handleResponse(BackendlessUser user) {
                                    // user has been logged

                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(editTextUsername.getWindowToken(), 0);

                                    if (saveLoginCheckBox.isChecked()) {
                                        username = editTextUsername.getText().toString();
                                        password = editTextPassword.getText().toString();
                                        loginPrefsEditor.putBoolean("saveLogin", true);
                                        loginPrefsEditor.putString("username", username);
                                        loginPrefsEditor.putString("password", encryption.encryptOrNull(password));
                                        loginPrefsEditor.commit();
                                    } else {
                                        loginPrefsEditor.clear();
                                        loginPrefsEditor.commit();
                                    }
                                    ringProgressDialog.dismiss();
                                    Intent loggedInIntent = new Intent(MainActivity.this, NavDrawerActivity.class);
                                    startActivity(loggedInIntent);
                                }

                                public void handleFault(BackendlessFault fault) {
                                    if (ringProgressDialog != null) {
                                        ringProgressDialog.dismiss();
                                    }
                                    // int errorCode = Integer.valueOf(fault.getCode());
                                    if (fault.getCode().equals("Internal client exception")) {
                                        Toast.makeText(getApplicationContext(),
                                                "Error unable to sign in. Please check your connection and try again later",
                                                Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Error " + fault.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "That password is incorrect. Try again or click 'forgot password' to receive a new password.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter your email address in the format someone@example.com.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class Save extends AsyncTask<Void, Integer, Void> {

        Person personCreated;
        BackendlessUser userCreated;

        public Save(Person person, BackendlessUser user) {
            personCreated = person;
            userCreated = user;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {

            BackendlessUser checkUser = Backendless.UserService.findById(userCreated.getObjectId());

            // First time user reg, set User property for person
            if (checkUser.getProperty("persons") == null) {
                Person p1 = Backendless.Data.of(Person.class).save(personCreated);
                userCreated.setProperty("persons", p1);
                userCreated.setProperty("ownerId", userCreated.getObjectId());
                userCreated.removeProperty("user-registered");
                userCreated.removeProperty("socialAccount");
                Backendless.UserService.update(userCreated);
            }
            // Just update person and
            else {

                Person pp = (Person) checkUser.getProperty("persons");
                pp.setGender((String) checkUser.getProperty("gender"));
                pp.setLname((String) checkUser.getProperty("lname"));
                pp.setFname((String) checkUser.getProperty("fname"));
                pp.setFullname(checkUser.getProperty("fname") + " " +
                        checkUser.getProperty("lname"));
                pp.setEmail((String) checkUser.getProperty("email"));
                pp.setSocial("Facebook");
                pp.pendingResponseSlot = null;
                pp.myPlans = null;
                pp.myCreatedSlot = null;
                pp.goingToSlot =null;
                Backendless.Persistence.of(Person.class).save(pp);
                userCreated.setProperty("persons", pp);
                userCreated.removeProperty("socialAccount");
                userCreated.removeProperty("user-registered");
                Backendless.UserService.update(userCreated);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextUsername.getWindowToken(), 0);
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
            Intent loggedInIntent = new Intent(MainActivity.this, NavDrawerActivity.class);
            startActivity(loggedInIntent);
        }
    }
}