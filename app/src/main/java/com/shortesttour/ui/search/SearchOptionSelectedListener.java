package com.shortesttour.ui.search;

import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.models.Place;

public interface SearchOptionSelectedListener {
    void onShowInMapClick(Place place);
    void onAddToListClick(Place place);
}
