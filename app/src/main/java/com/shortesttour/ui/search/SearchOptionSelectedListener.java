package com.shortesttour.ui.search;

import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.models.Place;

public interface SearchOptionSelectedListener {
    void onShowInMapClick(LatLng latLng,String placeTitle);
    void onAddToListClick(Place place);
}
