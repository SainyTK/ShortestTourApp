package com.shortesttour.events;

import com.shortesttour.models.Place;

public class AddPlaceEvent {
    private Place place;

    public AddPlaceEvent(Place place){
        this.place = place;
    }

    public Place getPlace(){
        return place;
    }
}
