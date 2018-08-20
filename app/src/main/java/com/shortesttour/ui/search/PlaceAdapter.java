package com.shortesttour.ui.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shortesttour.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.Holder> {

    private Context mContext;
    private String[] mPlaces;

    class Holder extends RecyclerView.ViewHolder{

        @BindView(R.id.text_place_title)
        TextView textPlaceTitle;
        @BindView(R.id.text_place_description)
        TextView textPlaceDescription;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void setData(int position){
            textPlaceTitle.setText(mPlaces[position]);
        }
    }

    public PlaceAdapter(Context context,String[] places){
        mContext = context;
        mPlaces = places;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_place_search,parent,false);
        Holder holder = new Holder(view);
        Log.d("Test", "onCreateViewHolder: size = " + mPlaces.length);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setData(position);
        Log.d("Test", "onBindViewHolder: position = " + position);
    }

    @Override
    public int getItemCount() {
        return mPlaces.length;
    }

    public void setDataSet(String[] dataSet){
        mPlaces = dataSet;
        notifyDataSetChanged();
    }

}
