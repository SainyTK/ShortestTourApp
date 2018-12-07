package com.shortesttour.utils;

import android.util.Log;
import android.util.TimeUtils;

import com.shortesttour.models.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestUtils {

    private static final String TAG = "TestUtils";
    private long startTime;
    private static TestUtils utils;

    private int MAX_ROUND = 15;
    private int NUM_PLACE = 60;

    private int round = 0;
    private int algor = 0;
    private List<Integer> randomIndex = new ArrayList<>();

    private String resultTable = "";

    public static TestUtils getInstance(){
        if(utils == null)
            utils = new TestUtils();
        return utils;
    }

    public void setStartTextNN(){
        resultTable = roundText() + "Nearest Neighbor\n" + tableHeadText();
    }

    public void setStartTextDP(){
        resultTable = roundText() + "Dynamic Programming\n" + tableHeadText();
    }

    public void showResultTable() {
        Log.d(TAG, "showResultTable: " + resultTable);
    }

    public void setPlaceList(List<Place> placeList){
        String placeText = "\nPlaceList = ";
        for(Place place : placeList)
            placeText += place.getPlaceTitle() + " ";
        resultTable += placeText + "\n";
    }

    public void setStartTime(){
        startTime = System.currentTimeMillis();
    }

    public long getRuntime(){
        return System.currentTimeMillis() - startTime;
    }

    public String roundText(){
        return "\nRound " + round + "\n";
    }


    public String tableHeadText() {
        return "Num of node\t\t\t\t\t\tDistance\t\t\t\t\tDuration\t\t\t\t\tRuntime\n";
    }

    public String resultRowText(int numNode, String distance, String duration, long runTime) {
        return numNode + "\t\t\t" + distance + "\t\t\t" + duration + "\t\t\t" + runTime + "\n";
    }

    public void updateResultTable(int numNode, String distance, String duration, long runTime) {
        resultTable = resultTable + resultRowText(numNode,distance,duration,runTime);
    }

    public void switchAlgorithm(){
        algor = algor == 0 ? 1 : 0;
    }

    public void addRound(){
        randomIndex.clear();
        round++;
    }

    public void setRandomIndex(){
        addRound();
        do{
            int r = new Random().nextInt(NUM_PLACE);
            if(randomIndex.contains(r))
                continue;
            randomIndex.add(r);
//            Log.d(TAG, "setRandomIndex: random = " + r);
        } while(randomIndex.size() < 15);
    }

    public List<Integer> getRandomIndex(){
        return randomIndex;
    }

    public void clearPlaces() {
        randomIndex.clear();
    }

    public String getResultTable(){
        return resultTable;
    }

    public int getRound(){
        return round;
    }

}
