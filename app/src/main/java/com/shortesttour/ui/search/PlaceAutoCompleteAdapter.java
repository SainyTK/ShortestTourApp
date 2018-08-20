package com.shortesttour.ui.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;


public class PlaceAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable{

    private String[] mDataSet;

    public PlaceAutoCompleteAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return mDataSet.length;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return mDataSet[position];
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if(charSequence != null){

                    filterResults.values = mDataSet;
                    filterResults.count = mDataSet.length;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if(filterResults != null && filterResults.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
