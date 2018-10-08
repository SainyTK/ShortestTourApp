package com.shortesttour.utils.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphNode {

    private List<List<HashMap<String, String>>> routes;
    private int distance;
    private int duration;

    public GraphNode(){
        routes = new ArrayList<>();
        distance = 0;
        duration = 0;
    }

    public List<List<HashMap<String, String>>> getRoutes() {
        return routes;
    }

    public void setRoutes(List<List<HashMap<String, String>>> routes) {
        this.routes = routes;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


}
