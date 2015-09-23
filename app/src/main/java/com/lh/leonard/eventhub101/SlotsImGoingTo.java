package com.lh.leonard.eventhub101;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TableRow;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.List;

public class SlotsImGoingTo extends Fragment {

    Contact contact;
    Person person;
    List<Slot> slot;
    BackendlessCollection<Person> persons;
    BackendlessCollection<Slot> slots;
    SearchView searchViewSlots;
    AutoResizeTextView textViewTextNoSlotAvaliable;
    RVAdapter adapter;
    private ProgressBar progressBar;

    BackendlessUser userLoggedIn = Backendless.UserService.CurrentUser();

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.slots_display, container, false);

        Backendless.Persistence.mapTableToClass("Person", Person.class);
        Backendless.Persistence.mapTableToClass("Contact", Contact.class);
        Backendless.Persistence.mapTableToClass("Slot", Slot.class);

        TextView textViewTitleSlotsDisplay = (TextView) v.findViewById(R.id.textViewTitleSlotsDisplay);
        textViewTextNoSlotAvaliable = (AutoResizeTextView) v.findViewById(R.id.textViewTextNoSlotAvaliable);

        textViewTitleSlotsDisplay.setText("Going To Events");

        final Typeface regularFont = Typeface.createFromAsset(v.getContext().getAssets(), "fonts/GoodDog.otf");


        textViewTextNoSlotAvaliable.setTypeface(regularFont);
        textViewTitleSlotsDisplay.setTypeface(regularFont);

        person = (Person) userLoggedIn.getProperty("persons");

        new ParseURL().execute();

        searchViewSlots = (SearchView) v.findViewById(R.id.searchViewSlots);

        searchViewSlots.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                                   public boolean onQueryTextChange(String text) {

                                                       if (adapter != null) {
                                                           if (TextUtils.isEmpty(text)) {
                                                               adapter.getFilter().filter("");
                                                           } else {
                                                               adapter.getFilter().filter(text.toString());
                                                           }
                                                       }
                                                       return true;
                                                   }

                                                   @Override
                                                   public boolean onQueryTextSubmit(String query) {
                                                       return false;
                                                   }
                                               }
        );

        return v;
    }

    private class ParseURL extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {

            StringBuilder whereClause = new StringBuilder();
            whereClause.append("Person[goingToSlot]");
            whereClause.append(".objectId='").append(person.getObjectId()).append("'");

            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
            dataQuery.setWhereClause(whereClause.toString());

            slots = Backendless.Data.of(Slot.class).find(dataQuery);
            slot = slots.getData();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isAdded()) {

                if (!slot.isEmpty()) {

                    RecyclerView rv = (RecyclerView) v.findViewById(R.id.rv);

                    rv.setHasFixedSize(true);
                    LinearLayoutManager llm = new LinearLayoutManager(v.getContext());
                    rv.setLayoutManager(llm);

                    Resources r = getResources();

                    adapter = new RVAdapter(slot, r);

                    rv.setAdapter(adapter);

                    rv.addOnItemTouchListener(new RecyclerItemClickListener(v.getContext(), rv, new RecyclerItemClickListener.OnItemClickListener() {

                        @Override
                        public void onItemClick(View view, int position) {

                            Intent slotDialogIntent = new Intent(getActivity(), SlotsImGoingToDialog.class);

                            slotDialogIntent.putExtra("slotRef", position);

                            startActivity(slotDialogIntent);
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                            //TODO: Dialog show, remove slot. Remove from list clear adapter, give adapter now list
                            //TODO Yes: get the ownerObjectId and remove from database
                        }
                    }
                    ));
                    progressBar.setVisibility(View.GONE);
                    TableRow rowSearchView = (TableRow) v.findViewById(R.id.rowSearchView);
                    rowSearchView.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.VISIBLE);

                } else {
                    progressBar.setVisibility(View.GONE);
                    textViewTextNoSlotAvaliable.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}