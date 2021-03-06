package com.lh.leonard.amplifiedscheduler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.persistence.BackendlessDataQuery;
import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyPlans extends AppCompatActivity implements
        WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    LinearLayout wrapperViews;
    Person personLoggedIn;
    List<Plan> slot;
    BackendlessCollection<Person> persons;
    BackendlessCollection<Plan> slots;
    BackendlessUser userLoggedIn = Backendless.UserService.CurrentUser();
    View v;
    private Menu optionsMenu;
    ProgressDialog ringProgressDialog;
    AlertDialog dialog;
    AgendaCalendarView mAgendaCalendarView;
    List<CalendarEvent> eventList;
    Boolean loadingPage = true;
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        mAgendaCalendarView = (AgendaCalendarView) findViewById(R.id.agenda_calendar_view);
        Backendless.Data.mapTableToClass("Plan", Plan.class);
        Backendless.Data.mapTableToClass("Person", Person.class);

        final Typeface RobotoCondensedLightItalic = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/RobotoCondensed-LightItalic.ttf");


        AutoResizeTextView tvSocial = (AutoResizeTextView) findViewById(R.id.textViewSocial);
        AutoResizeTextView textViewCulturalEvents = (AutoResizeTextView) findViewById(R.id.textViewCulturalEvents);
        AutoResizeTextView textViewAcademicEvents = (AutoResizeTextView) findViewById(R.id.textViewAcademicEvents);
        AutoResizeTextView textViewWorkEvents = (AutoResizeTextView) findViewById(R.id.textViewWorkEvents);
        wrapperViews = (LinearLayout) findViewById(R.id.wrapperViews);
        assert tvSocial != null;
        tvSocial.setTypeface(RobotoCondensedLightItalic);
        assert textViewCulturalEvents != null;
        textViewCulturalEvents.setTypeface(RobotoCondensedLightItalic);
        assert textViewAcademicEvents != null;
        textViewAcademicEvents.setTypeface(RobotoCondensedLightItalic);
        assert textViewWorkEvents != null;
        textViewWorkEvents.setTypeface(RobotoCondensedLightItalic);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);
        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        assert mWeekView != null;
        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(this); // do nothing for now
        mWeekView.setMonthChangeListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (userLoggedIn.getProperty("persons") != null)
            personLoggedIn = (Person) userLoggedIn.getProperty("persons");
        new ParseURL().execute();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    //  @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
//        dialog = new AlertDialog.Builder(getApplicationContext())
//                .setTitle("Go to event")
//                .setMessage("You about to go to " + slot.get(position).getSubject())
//                .setPositiveButton("GOING", new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int whichButton) {
//
//                        dialog.dismiss();
//                        ringProgressDialog = ProgressDialog.show(getApplicationContext(), "Please wait ...",
//                                "Going to " + slot.get(position).getSubject() + " ...", true);
//                        ringProgressDialog.setCancelable(false);
//                        new GoingToEvent(position).execute();
//                    }
//                })
//                .setNegativeButton("NOT GOING", new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        dialog.dismiss();
//                        ringProgressDialog = ProgressDialog.show(getApplicationContext(), "Please wait ...",
//                                "Not going to" + slot.get(position).getSubject() + " ...", true);
//                        ringProgressDialog.setCancelable(false);
//                        new NotGoingToEvent(position).execute();
//                    }
//                }).setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        dialog.dismiss();
//                    }
//                }).show();
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     *
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MyPlans Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.lh.leonard.amplifiedscheduler/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MyPlans Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.lh.leonard.amplifiedscheduler/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Intent slotDialogIntent = new Intent(MyPlans.this, MyPlansDialog.class);
        int position = Integer.parseInt(String.valueOf(event.getId()));
        slotDialogIntent.putExtra("objectId", String.valueOf(slot.get(position).getObjectId()));
        startActivity(slotDialogIntent);
    }

    /**
     * Checks if an event falls into a specific year and month.
     *
     * @param event The event to check for.
     * @param year  The year.
     * @param month The month.
     * @return True if the event matches the year and month.
     */
    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {

    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        if (slot != null) {
            if (!slot.isEmpty()) {
                int i = 0;
                for (Plan event : slot) {


                    WeekViewEvent weekViewEvent = new WeekViewEvent(Long.parseLong(String.valueOf(i)), event.getSubject(),
                            (String) event.getLocation().getMetadata("address"),
                            event.getStartCalendar(), event.getEndCalendar());

                    if (event.getLocation().getMetadata("category").equals("Social Event")) {
                        weekViewEvent.setColor(getResources().getColor(R.color.green));
                    } else if (event.getLocation().getMetadata("category").equals("Work Event")) {
                        weekViewEvent.setColor(getResources().getColor(R.color.orange));
                    } else if (event.getLocation().getMetadata("category").equals("Cultural Event")) {
                        weekViewEvent.setColor(getResources().getColor(R.color.wallet_holo_blue_light));
                    } else if (event.getLocation().getMetadata("category").equals("Academic Event")) {
                        weekViewEvent.setColor(getResources().getColor(R.color.purple));
                    }
                    i++;
                    if (eventMatches(weekViewEvent, newYear, newMonth)) {
                        events.add(weekViewEvent);
                    }
                }
            }
        }

        return events;
    }


    private class ParseURL extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {

            String whereClause = "Person[myPlans]" +
                    ".objectId='" + personLoggedIn.getObjectId() + "'";

            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
            dataQuery.setWhereClause(whereClause);

            slots = Backendless.Data.of(Plan.class).find(dataQuery);
            slot = slots.getData();

            eventList = new ArrayList<>();

            Calendar now = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            now.setTimeZone(tz);

            getEventsFromList(slot);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            mWeekView.notifyDatasetChanged();

            CalendarPickerController mPickerController = new CalendarPickerController() {
                @Override
                public void onDaySelected(DayItem dayItem) {
                }

                @Override
                public void onEventSelected(CalendarEvent event) {

                    if (!event.getTitle().equals("No events")) {

                        //TODO fix this
                        Intent slotDialogIntent = new Intent(MyPlans.this, MyPlansDialog.class);

                        int position = Integer.parseInt(String.valueOf(event.getId()));
                        slotDialogIntent.putExtra("origin", 2);
                        slotDialogIntent.putExtra("objectId", String.valueOf(slot.get(position).getObjectId()));

                        startActivity(slotDialogIntent);
                    }
                }

                @Override
                public void onScrollToDate(Calendar calendar) {

                }
            };

            Calendar minDate;
            Calendar maxDate;
            // minimum and maximum date of our calendar
            // 2 month behind, one year ahead, example: March 2015 <-> May 2015 <-> May 2016
            minDate = Calendar.getInstance();
            maxDate = Calendar.getInstance();

            minDate.add(Calendar.MONTH, -1);
            minDate.set(Calendar.DAY_OF_MONTH, 1);
            maxDate.add(Calendar.YEAR, 1);

            mAgendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), mPickerController);
            // mWeekView.setVisibility(View.VISIBLE);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            RelativeLayout RLProgressBar = (RelativeLayout) findViewById(R.id.RLProgressBar);
            assert RLProgressBar != null;
            RLProgressBar.setVisibility(View.GONE);
            assert progressBar != null;
            progressBar.setVisibility(View.GONE);
            wrapperViews.setVisibility(View.VISIBLE);

            loadingPage = false;
        }
    }

//    public void setRefreshActionButtonState(final boolean refreshing) {
//        if (optionsMenu != null) {
//            final MenuItem refreshItem = optionsMenu
//                    .findItem(R.id.action_refresh);
//            if (refreshItem != null) {
//                if (refreshing) {
//                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
//                } else {
//                    refreshItem.setActionView(null);
//                }
//            }
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (!loadingPage) {
            int id = item.getItemId();

            setupDateTimeInterpreter(id == R.id.action_week_view);
            switch (id) {
                case R.id.action_today:
                    mWeekView.goToToday();
                    return true;
                case R.id.action_day_view:
                    if (mWeekViewType != TYPE_DAY_VIEW) {
                        item.setChecked(!item.isChecked());
                        mWeekViewType = TYPE_DAY_VIEW;
                        mWeekView.setNumberOfVisibleDays(1);

                        // Lets change some dimensions to best fit the view.
                        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                        return true;
                    }
                    return true;
                case R.id.action_three_day_view:
                    if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                        item.setChecked(!item.isChecked());
                        mWeekViewType = TYPE_THREE_DAY_VIEW;
                        mWeekView.setNumberOfVisibleDays(3);

                        // Lets change some dimensions to best fit the view.
                        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                        return true;
                    }
                    return true;
                case R.id.action_week_view:
                    if (mWeekViewType != TYPE_WEEK_VIEW) {
                        item.setChecked(!item.isChecked());
                        mWeekViewType = TYPE_WEEK_VIEW;
                        mWeekView.setNumberOfVisibleDays(7);

                        // Lets change some dimensions to best fit the view.
                        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                        return true;
                    }
                case R.id.action_switch:
                    invalidateOptionsMenu();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        if (!mWeekView.isShown()) {
            inflater.inflate(R.menu.menu_week_view, menu);
            mAgendaCalendarView.setVisibility(View.GONE);
            mWeekView.setVisibility(View.VISIBLE);
        } else {
            inflater.inflate(R.menu.menu_events, menu);
            mWeekView.setVisibility(View.GONE);
            mAgendaCalendarView.setVisibility(View.VISIBLE);
        }
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.share);

        // Fetch and store ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Hey! Check out this free event/personal planner app: https://play.google.com/store/apps/details?id=com.lh.leonard.amplifiedscheduler");
        sendIntent.setType("text/plain");
        mShareActionProvider.setShareIntent(sendIntent);

        return super.onCreateOptionsMenu(menu);
    }

    private void getEventsFromList(List<Plan> eventListSlots) {

        for (int i = 0; i < eventListSlots.size(); i++) {

            Boolean allDay = false;

            if (eventListSlots.get(i).isAllDayEvent()) {
                allDay = true;
            }

            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();

            startTime.set(Calendar.DAY_OF_YEAR, eventListSlots.get(i).getStartCalendar().get(Calendar.DAY_OF_YEAR));

            endTime.set(Calendar.DAY_OF_YEAR, eventListSlots.get(i).getEndCalendar().get(Calendar.DAY_OF_YEAR));

            int color = ContextCompat.getColor(this, R.color.green);

            if (eventListSlots.get(i).getLocation().getMetadata("category").equals("Social Event")) {
                color = ContextCompat.getColor(this, R.color.green);
            } else if (eventListSlots.get(i).getLocation().getMetadata("category").equals("Work Event")) {
                color = ContextCompat.getColor(this, R.color.orange);
            } else if (eventListSlots.get(i).getLocation().getMetadata("category").equals("Cultural Event")) {
                color = ContextCompat.getColor(this, R.color.wallet_holo_blue_light);
            } else if (eventListSlots.get(i).getLocation().getMetadata("category").equals("Academic Event")) {
                color = ContextCompat.getColor(this, R.color.purple);
            }

            String location = (String) eventListSlots.get(i).getLocation().getMetadata("address");
            BaseCalendarEvent event = new BaseCalendarEvent(eventListSlots.get(i).getSubject(),
                    eventListSlots.get(i).getNote(), location,
                    color, startTime, endTime, allDay);

            event.setId(Long.parseLong(String.valueOf(i)));
            eventList.add(event);
        }
    }

//    private class Refresh extends AsyncTask<Void, Integer, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            StringBuilder whereClause = new StringBuilder();
//            whereClause.append("Person[mycreatedslot]");
//            whereClause.append(".objectId='").append(personLoggedIn.getObjectId()).append("'");
//
//            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
//            dataQuery.setWhereClause(whereClause.toString());
//
//            slots = Backendless.Data.of(Slot.class).find(dataQuery);
//            slot = slots.getData();
//
//
//            eventList = new ArrayList<>();
//
//            getEventsFromList(slot);
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//
//            CalendarPickerController mPickerController = new CalendarPickerController() {
//                @Override
//                public void onDaySelected(DayItem dayItem) {
//                }
//
//                @Override
//                public void onEventSelected(CalendarEvent event) {
//
//                    if (!event.getTitle().equals("No events")) {
//
//                        Intent slotDialogIntent = new Intent(MyCreatedSlots.this, MyCreatedSlotsDialog.class);
//
//                        int position = Integer.parseInt(String.valueOf(event.getId()));
//
//
//                        slotDialogIntent.putExtra("objectId", String.valueOf(slot.get(position).getObjectId()));
//
//                        startActivity(slotDialogIntent);
//
//                    }
//                }
//            };
//
//
//            mAgendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), mPickerController);
//            Toast.makeText(getApplicationContext(), "Events Synced", Toast.LENGTH_LONG).show();
//            setRefreshActionButtonState(false);
//        }
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, NavDrawerActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void invalidateOptionsMenu() {

        super.invalidateOptionsMenu();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }
}