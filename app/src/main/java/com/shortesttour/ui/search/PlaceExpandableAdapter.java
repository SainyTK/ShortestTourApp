package com.shortesttour.ui.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.shortesttour.R;
import com.shortesttour.events.AddPlaceEvent;
import com.shortesttour.events.ShowInMapEvent;
import com.shortesttour.models.Place;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class PlaceExpandableAdapter extends ExpandableRecyclerAdapter<Place,String,PlaceViewHolder,OptionViewHolder>{

    private List<Place> places;
    private Context mContext;
    private int allDataCount;

    public PlaceExpandableAdapter(Context context, @NonNull List<Place> placeList) {
        super(placeList);
        mContext = context;
        places = placeList;
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
    public void onBindParentViewHolder(@NonNull PlaceViewHolder parentViewHolder, int parentPosition, @NonNull Place parent) {
        if(parentPosition < places.size())
            parentViewHolder.bind(places.get(parentPosition));
    }

    @Override
    public void onBindChildViewHolder(@NonNull OptionViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull String child) {
        childViewHolder.setOptionClickListener(new OptionViewHolder.OptionClickListener() {
            @Override
            public void clickShowInMap(int parentPosition) {
                Place place = places.get(parentPosition);
                EventBus.getDefault().post(new ShowInMapEvent(place));
            }

            @Override
            public void clickAddToList(int parentPosition) {
                Place place = places.get(parentPosition);
                EventBus.getDefault().post(new AddPlaceEvent(place));
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

    public void modifyData(List<Place> newData){
        places = newData;
        notifyDataSetChanged();
        notifyParentDataSetChanged(false);
    }

}