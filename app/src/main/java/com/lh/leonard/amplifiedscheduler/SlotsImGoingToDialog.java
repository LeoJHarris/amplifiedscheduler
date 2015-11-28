package com.lh.leonard.amplifiedscheduler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ProgressBar;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlotsImGoingToDialog extends Activity {

    BackendlessUser userLoggedIn = Backendless.UserService.CurrentUser();
    List<Slot> slotsList;
    AutoResizeTextView textViewSubject;
    AutoResizeTextView textViewMessage;
    AutoResizeTextView textViewDateAndTime;
    AutoResizeTextView textViewLocation;
    AutoResizeTextView textViewMyeventSpacesAvaliable;
    String objectId;
    Button buttonGoingToEventNotGoing;
    List<Person> personsToSms;
    BackendlessCollection<Person> personsToSmsCollection;
    Button buttonGoingToEventParticipantsSlot;
    Person person;
    BackendlessCollection<Slot> slots;
    SpannableString content;
    ProgressBar progressBar;
    String eventRemoved;
    Slot event;
    AlertDialog dialog;
    ProgressDialog ringProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_created_slots_dialog);

        Backendless.Data.mapTableToClass("Slot", Slot.class);
        Backendless.Data.mapTableToClass("Person", Person.class);
        Backendless.Persistence.mapTableToClass("Slot", Slot.class);
        Backendless.Persistence.mapTableToClass("Person", Person.class);

        final Typeface RobotoBlack = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Roboto-Black.ttf");
        final Typeface RobotoCondensedLightItalic = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RobotoCondensed-LightItalic.ttf");
        final Typeface RobotoCondensedLight = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RobotoCondensed-Light.ttf");
        final Typeface RobotoCondensedBold = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RobotoCondensed-Bold.ttf");

        textViewSubject = (AutoResizeTextView) findViewById(R.id.textViewMySlotSubject);
        textViewMessage = (AutoResizeTextView) findViewById(R.id.textViewMySlotMessage);
        textViewDateAndTime = (AutoResizeTextView) findViewById(R.id.textViewMySlotDateAndTime);
        textViewLocation = (AutoResizeTextView) findViewById(R.id.textViewMySlotLocation);
        textViewMyeventSpacesAvaliable = (AutoResizeTextView) findViewById(R.id.textViewMyEventSpacesAvaliable);
        buttonGoingToEventNotGoing = (Button) findViewById(R.id.buttonMySlotCancelSlot);
        buttonGoingToEventParticipantsSlot = (Button) findViewById(R.id.buttonGoingToEventParticipantsSlot);

        textViewSubject.setTypeface(RobotoCondensedLight);
        textViewMessage.setTypeface(RobotoCondensedLight);
        textViewDateAndTime.setTypeface(RobotoCondensedLight);
        textViewLocation.setTypeface(RobotoCondensedLight);
        textViewMyeventSpacesAvaliable.setTypeface(RobotoCondensedLight);
        buttonGoingToEventNotGoing.setTypeface(RobotoCondensedLight);
        buttonGoingToEventParticipantsSlot.setTypeface(RobotoCondensedLight);

        Backendless.Data.mapTableToClass("Slot", Slot.class);
        Backendless.Data.mapTableToClass("Person", Person.class);

        person = (Person) userLoggedIn.getProperty("persons");

        new LoadMyContacts().execute();

        buttonGoingToEventNotGoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                ringProgressDialog = ProgressDialog.show(SlotsImGoingToDialog.this, "Please wait ...", "Adding " + event.getSubject() + " ...", true);
                ringProgressDialog.setCancelable(false);
                new NotGoingToEvent().execute();

            }
        });

        buttonGoingToEventParticipantsSlot.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {

                                                                      Intent participantsIntent = new Intent(SlotsImGoingToDialog.this, ParticipantsActivity.class);

                                                                      participantsIntent.putExtra("eventid", event.getObjectId());

                                                                      startActivity(participantsIntent);
                                                                  }
                                                              }
        );

        textViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent mapIntent = new Intent(SlotsImGoingToDialog.this, JustMapActivity.class);

                mapIntent.putExtra("lat", event.getLocation().getLatitude());
                mapIntent.putExtra("long", event.getLocation().getLongitude());
                mapIntent.putExtra("subject", event.getSubject());
                startActivity(mapIntent);
            }
        });
    }

    private class LoadMyContacts extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Void... params) {

            Bundle data = getIntent().getExtras();
            objectId = data.getString("objectId");

            event = Backendless.Data.of(Slot.class).findById(objectId);


            return (String) event.getLocation().getMetadata("address");

        }

        @Override
        protected void onPostExecute(String addresses) {


            if (event.getSubject() != null) {
                textViewSubject.setText(event.getSubject());
            }

            if (event.getMessage() != null) {
                textViewMessage.setText("Message: " + event.getMessage());
            }

            if (event.getStartCalendar() != null) {


                textViewDateAndTime.setText("When " + event.getStartCalendar().getTime());
//                    if (event.getEnd() == null) {
//                        textViewDateAndTime.setText("When: " + event.getDateofslot() + " @ " + event.getStart());
//
//                    } getEnd

            }

            if (event.getMaxattendees() != 0) {


                Integer spacesAvaliable = event.getMaxattendees();
                Integer going = event.getAttendees().size();
                {
                    Integer spacesLeft = spacesAvaliable - event.getAttendees().size();
                    textViewMyeventSpacesAvaliable.setText(going + " going, waiting response from " + (spacesAvaliable - going));

                }

            }

            if (event.getLocation() != null) {
                content = new SpannableString("Where: " + (String) event.getLocation().getMetadata("address"));
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                textViewLocation.setText(content); //TODO Button to get Location else just Text
            }
            progressBar = (ProgressBar) findViewById(R.id.progressBarMyCreatedSlotsDialog);
            progressBar.setVisibility(View.GONE);

            textViewMyeventSpacesAvaliable.setVisibility(View.VISIBLE);
            textViewSubject.setVisibility(View.VISIBLE);
            textViewMessage.setVisibility(View.VISIBLE);
            textViewLocation.setVisibility(View.VISIBLE);
            textViewDateAndTime.setVisibility(View.VISIBLE);
            buttonGoingToEventNotGoing.setVisibility(View.VISIBLE);
            buttonGoingToEventParticipantsSlot.setVisibility(View.VISIBLE);
        }
    }

    private class NotGoingToEvent extends AsyncTask<Void, Integer, Void> {

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

            sendsmss(event.getPhone(), person.getFullname(), event.subject,
                    event.getStartCalendar().getTime().toString(), event.getStartCalendar().getTime().toString());

            Map<String, String> args = new HashMap<>();
            args.put("id", "removeevent");

            args.put("objectIdPerson",person.getObjectId());

            args.put("event", event.getObjectId());

            Backendless.Events.dispatch("ManageEvent", args, new AsyncCallback<Map>() {
                @Override
                public void handleResponse(Map map) {
                    dialog.dismiss();
                    onBackPressed();

                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                    dialog.dismiss();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    @JavascriptInterface
    public void sendsmss(String phoneNumber, String from, String subject, String date, String place) {

        String messageSubString = "Automated TXT - Amplified Scheduler: " + subject + " on the " + date + " at " + place + " was cancelled by ";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, messageSubString, null, null);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MyCreatedSlots.class);
        startActivity(intent);
        finish();
    }
}