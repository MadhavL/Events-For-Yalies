package com.example.custom_listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListAdapter extends ArrayAdapter<Event> {
    private Filter filter;
    ArrayList<Event> eventArrayList;
    int numFiltered;
    boolean ready;


    public ListAdapter(Context context, ArrayList<Event> eventArrayList){

        super(context,R.layout.list_item, eventArrayList);
        this.eventArrayList = eventArrayList;
        this.numFiltered = 999;
        this.ready = false;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Event event = getItem(position);

        if (convertView == null){

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);

        }

        ImageView imageView = convertView.findViewById(R.id.profile_pic);
        TextView eventName = convertView.findViewById(R.id.eventName);
        TextView eventTime = convertView.findViewById(R.id.eventTime);

        imageView.setImageBitmap(event.image);
        eventName.setText(event.name);
        if (event.time != null) {
            eventTime.setText(event.time);
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new UserFilter(eventArrayList);
        return filter;
    }

    /**
     * Class for filtering in Arraylist listview. Objects need a valid
     * 'toString()' method.
     *
     * @author Tobias Schürg inspired by Alxandr
     *         (http://stackoverflow.com/a/2726348/570168)
     *
     */
    private class UserFilter extends Filter {

        private ArrayList<Event> sourceObjects;

        public UserFilter(List<Event> objects) {
            sourceObjects = new ArrayList<Event>();
            synchronized (this) {
                sourceObjects.addAll(eventArrayList);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq[] = chars.toString().toLowerCase().split("-");
            String dateFilter = filterSeq[0];
            String categoryFilter = "";
            List<String> categoryList = new ArrayList<String>();

            if (filterSeq.length > 1) {
                categoryFilter = filterSeq[1];
                String categoryListArray[] = categoryFilter.toLowerCase().split(";");
                categoryList = Arrays.asList(categoryListArray);
            }

            FilterResults result = new FilterResults();
            if (filterSeq != null && (dateFilter.length() > 0)) {
                ArrayList<Event> filter = new ArrayList<Event>();
                System.out.println(chars);
                for (Event object : sourceObjects) {
                    // the filtering itself:
//                    System.out.println("Name: " + object.name);
                    if (filterSeq.length > 1) {
                        if (object.date.toLowerCase().contains(dateFilter)) {
                            for (String category : categoryList) {
                                if (object.category.contains(category)) {
                                    filter.add(object);
                                }
                            }
                        }


                    }
                    else {
                        if (object.date.toLowerCase().contains(dateFilter))
                            filter.add(object);
                    }

                }
                result.count = filter.size();
                result.values = filter;
            } else {
                // add all objects
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<Event> filtered = (ArrayList<Event>) results.values;
            numFiltered = results.count;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                add((Event) filtered.get(i));
            notifyDataSetInvalidated();
        }

    }
}
