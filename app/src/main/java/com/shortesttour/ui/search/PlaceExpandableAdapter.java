package com.shortesttour.ui.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.shortesttour.R;

import java.util.List;

public class PlaceExpandableAdapter extends ExpandableRecyclerAdapter<PlaceTitle,String,PlaceTitleViewHolder,OptionViewHolder>{

    List<PlaceTitle> placesTitle;
    Context mContext;

    public PlaceExpandableAdapter(Context context, @NonNull List<PlaceTitle> parentList) {
        super(parentList);
        mContext = context;
        placesTitle = parentList;
    }

    @NonNull
    @Override
    public PlaceTitleViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_place_search,parentViewGroup,false);
        return new PlaceTitleViewHolder(view);
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_place_option,childViewGroup,false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(@NonNull PlaceTitleViewHolder parentViewHolder, int parentPosition, @NonNull PlaceTitle parent) {
        parentViewHolder.bind(placesTitle.get(parentPosition));
    }

    @Override
    public void onBindChildViewHolder(@NonNull OptionViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull String child) {

    }

    public void modifyData(List<PlaceTitle> newData){
        placesTitle = newData;
        notifyDataSetChanged();
    }

}