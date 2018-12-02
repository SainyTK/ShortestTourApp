package com.shortesttour.events;

public class SwapPlaceEvent {
    private int fromPosition;
    private int toPosition;

    public SwapPlaceEvent(int from,int to){
        fromPosition = from;
        toPosition = to;
    }

    public int getFromPosition(){
        return fromPosition;
    }

    public int getToPosition(){
        return toPosition;
    }
}
