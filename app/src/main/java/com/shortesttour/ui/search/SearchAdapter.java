package com.shortesttour.ui.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SearchAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int resId;
    private String[] dataSet;

    public SearchAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        mContext = context;
        resId = resource;
        dataSet = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return null;
    }
}
