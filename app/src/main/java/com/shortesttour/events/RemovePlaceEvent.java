package com.shortesttour.events;

import com.shortesttour.models.Place;

public class RemovePlaceEvent {
    private int position;

    public RemovePlaceEvent(int position){
        this.position = position;
    }

    public int getPosition(){
        return position;
    }
}
