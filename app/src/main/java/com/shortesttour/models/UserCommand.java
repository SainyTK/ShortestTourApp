package com.shortesttour.models;

public class UserCommand{
    private String command;
    private Place place;
    private int position;

    public UserCommand(String command,Place place){
        this.command = command;
        this.position = 0;
        this.place = place;
    }

    public UserCommand(String command,int position){
        this.command = command;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}