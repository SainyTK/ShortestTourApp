package com.shortesttour.ui.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.shortesttour.R;

import java.util.List;

public class PlaceExpandableAdapter extends ExpandableRecyclerAdapter<PlaceParent,String,PlaceViewHolder,OptionViewHolder>{

    List<PlaceParent> places;
    Context mContext;
    int allDataCount;

    SearchOptionSelectedListener mListener;

    public PlaceExpandableAdapter(Context context, @NonNull List<PlaceParent> parentList) {
        super(parentList);
        mContext = context;
        places = parentList;
        allDataCount = places.size();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_place_search,parentViewGroup,false);
        return new PlaceViewHolder(view);
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_place_option,childViewGroup,false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(@NonNull PlaceViewHolder parentViewHolder, int parentPosition, @NonNull PlaceParent parent) {
        if(parentPosition < places.size())
            parentViewHolder.bind(places.get(parentPosition));
    }

    @Override
    public void onBindChildViewHolder(@NonNull OptionViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull String child) {
        childViewHolder.setOptionClickListener(new OptionViewHolder.OptionClickListener() {
            @Override
            public void clickShowInMap(int parentPosition) {
                if(mListener!=null){
                    PlaceParent place = places.get(parentPosition);
                    mListener.onShowInMapClick(place.getPlaceLatLng(),place.getPlaceTitle());
                }
            }

            @Override
            public void clickAddToList(int parentPosition) {
                if(mListener!=null){
                    PlaceParent placeParent = places.get(parentPosition);
                    mListener.onAddToListClick(placeParent.getPlace());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(super.getItemCount() == allDataCount){
            return places.size();
        }
        else
            return places.size()+1;
    }

    public void modifyData(List<PlaceParent> newData){
        places = newData;
        notifyDataSetChanged();
        notifyParentDataSetChanged(false);
    }

    public void setOptionSelectedListener(SearchOptionSelectedListener listener){
        mListener = listener;
    }

}