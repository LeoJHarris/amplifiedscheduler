package com.lh.leonard.amplifiedscheduler;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class ForgotPasswordReset extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_reset);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final EditText emailPasswordRecovery = (EditText) findViewById(R.id.emailPasswordRecovery);
        Button passwordwordRecoveryButton = (Button) findViewById(R.id.buttonEmailNewPassword);
        AutoResizeTextView editTextNoticeUpdate = (AutoResizeTextView) findViewById(R.id.editTextNoticeUpdate);

        final Typeface RobotoBlack = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "fonts/Roboto-Black.ttf");
        final Typeface RobotoCondensedLightItalic = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "fonts/RobotoCondensed-LightItalic.ttf");
        final Typeface RobotoCondensedLight = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "fonts/RobotoCondensed-Light.ttf");
        final Typeface RobotoCondensedBold = Typeface.createFromAsset(this.getApplicationContext().getAssets(), "fonts/RobotoCondensed-Bold.ttf");

        emailPasswordRecovery.setTypeface(RobotoCondensedLight);
        passwordwordRecoveryButton.setTypeface(RobotoCondensedLight);
        editTextNoticeUpdate.setTypeface(RobotoCondensedLightItalic);

        passwordwordRecoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String emailForPasswordRecovery = emailPasswordRecovery.getText().toString();

                if (!emailForPasswordRecovery.toString().toString().isEmpty()) {

                    Backendless.UserService.restorePassword(emailForPasswordRecovery, new AsyncCallback<Void>() {
                        @Override
                        public void handleResponse(Void aVoid) {
                            Toast.makeText(ForgotPasswordReset.this, "New Email sent to " + emailForPasswordRecovery,
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            System.out.println("Server reported an error - " + backendlessFault.getMessage());
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your email address.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
