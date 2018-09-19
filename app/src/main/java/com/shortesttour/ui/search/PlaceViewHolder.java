package com.shortesttour.ui.search;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.shortesttour.R;
import com.shortesttour.models.Place;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlaceViewHolder extends ParentViewHolder {

    @BindView(R.id.text_place_title)
    TextView textPlaceTitle;

    public PlaceViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void bind(Place place){
        textPlaceTitle.setText(place.getPlaceTitle());
    }

}
