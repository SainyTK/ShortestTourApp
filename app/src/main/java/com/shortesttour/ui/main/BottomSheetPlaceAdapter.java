package com.shortesttour.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shortesttour.R;
import com.shortesttour.models.Place;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BottomSheetPlaceAdapter extends RecyclerView.Adapter<BottomSheetPlaceAdapter.Holder>{

    public List<Place> mPlaceList;

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_place_title)
        TextView textPlaceTitle;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bindData(int position){
            textPlaceTitle.setText(mPlaceList.get(position).getPlaceTitle());
        }
    }

    public BottomSheetPlaceAdapter(List<Place> placeList){
        mPlaceList = placeList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_bottomsheet_place_list,parent,false);

        return new Holder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return mPlaceList.size();
    }

    public void setData(List<Place> placeList){
        mPlaceList = placeList;
        notifyDataSetChanged();
    }

    public void addPlace(Place place){
        mPlaceList.add(place);
        notifyItemChanged(getItemCount()-1);
    }

    public void removePlace(int position){
        mPlaceList.remove(position);
        notifyItemRemoved(position);
    }
}
