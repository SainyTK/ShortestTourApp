package com.shortesttour.utils.graph.Algorithms;

import android.util.Log;

import com.shortesttour.utils.FindPathUtils;
import com.shortesttour.utils.graph.GraphNode;

public abstract class NearestNeighbor {

    private FindPathUtils utils;

    public NearestNeighbor(FindPathUtils utils){
        this.utils = utils;
    }

    public int[] createPath(int[][] distance){
        int[] path = new int[distance.length+1];
        boolean[] visited = createVisited(distance.length);

        visited[0] = true;
        path[0] = 0;

        onProgress(0);

        int next = 0;
        for(int i=1;i<distance.length;i++){
            if(utils.checkCancel()) return null;

            next = findMinDestination(distance[next],visited);
            path[i] = next;
            visited[next] = true;

            onProgress((i*50/distance.length));
            Log.d("test", "createPath: next = " + next);
        }

        path[distance.length] = 0;
        onProgress(50);

        return path;
    }

    private int findMinDestination(int[] des,boolean[] visited){

        //initialize
        int minDesIdx = 0;
        for(int i = 0;i<des.length;i++){
            if(!visited[i]&&des[i]!=-1) {
                minDesIdx = i;
                break;
            }
        }
        //calculate
        for(int i=0;i<des.length;i++){
            int dis = des[i];
            int currentMin = des[minDesIdx];
            if(!visited[i]&&dis!=-1 && dis<=currentMin)
                minDesIdx = i;
        }
        return minDesIdx;
    }

    private boolean[] createVisited(int size){
        boolean[] visited = new boolean[size];
        for(int i=0;i<size;i++)
            visited[i] = false;
        return visited;
    }

    public abstract void onProgress(int value);
}
