package com.shortesttour.ui.search;

import com.google.android.gms.maps.model.LatLng;

public interface SearchOptionSelectedListener {
    void showInMap(LatLng latLng,String placeTitle);
}
