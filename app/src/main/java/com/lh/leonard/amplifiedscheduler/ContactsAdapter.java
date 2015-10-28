package com.lh.leonard.amplifiedscheduler;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leonard on 17/07/2015.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> implements Filterable {

    Resources resources;
    private List<Person> listSlots;
    Typeface regularFont;
    private List<Person> orig;

    public ContactsAdapter(List<Person> list, Resources R) {

        resources = R;
        listSlots = list;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contacts_card_view, viewGroup, false);

        regularFont = Typeface.createFromAsset(v.getContext().getAssets(), "fonts/GoodDog.otf");

        ContactViewHolder pvh = new ContactViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder slotViewHolder, int i) {

        slotViewHolder.personsFullName.setText(listSlots.get(i).getFname() + " " + listSlots.get(i).getLname());
        slotViewHolder.personEmail.setText("Country: " + listSlots.get(i).getCountry());
        if (listSlots.get(i).getContacts() != null) {
            if (!(listSlots.get(i).getContacts().isEmpty())) {
                slotViewHolder.personsFacebook.setText(String.valueOf("Contacts: " + listSlots.get(i).getContacts().size()));
            }
            slotViewHolder.personsFacebook.setText(String.valueOf("Contacts: " + String.valueOf(0)));
        } else {
            slotViewHolder.personsFacebook.setText(String.valueOf("Contacts: " + String.valueOf(0)));
        }
        slotViewHolder.personCountry.setText("Last Activity: " + listSlots.get(i).getUpdated());

        slotViewHolder.personCountry.setTypeface(regularFont);
        slotViewHolder.personsFacebook.setTypeface(regularFont);
        slotViewHolder.personLatestActivity.setTypeface(regularFont);
        slotViewHolder.personsFullName.setTypeface(regularFont);
        slotViewHolder.personEmail.setTypeface(regularFont);

        //personViewHolder.personPhoto.setImageResource(listTeachers.get(i).getPhoto());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return listSlots.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final List<Person> results = new ArrayList<Person>();
                if (orig == null)
                    orig = listSlots;
                if (constraint != null) {
                    if (orig != null & orig.size() > 0) {
                        for (final Person g : orig) {
                            if (g.getLname().toLowerCase().contains(constraint.toString()) || g.getFname().toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listSlots = (ArrayList<Person>) results.values;
                notifyDataSetChanged();

            }
        };
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        AutoResizeTextView personsFullName;
        AutoResizeTextView personsFacebook;
        AutoResizeTextView personEmail;
        AutoResizeTextView personLatestActivity;
        AutoResizeTextView personCountry;

        ContactViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personsFullName = (AutoResizeTextView) itemView.findViewById(R.id.person_fullname);
            personEmail = (AutoResizeTextView) itemView.findViewById(R.id.person_email);
            personLatestActivity = (AutoResizeTextView) itemView.findViewById(R.id.person_last_activity); //
            personsFacebook = (AutoResizeTextView) itemView.findViewById(R.id.person_facebook);
            personCountry = (AutoResizeTextView) itemView.findViewById(R.id.person_country);
            //personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
        }
    }

}