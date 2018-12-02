package com.shortesttour.events;

import com.shortesttour.models.Place;

public class ShowInMapEvent {
    private Place place;

    public ShowInMapEvent(Place place){
        this.place = place;
    }

    public Place getPlace(){
        return place;
    }
}
