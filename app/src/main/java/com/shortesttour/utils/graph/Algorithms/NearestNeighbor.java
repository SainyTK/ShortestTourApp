package com.shortesttour.utils.graph.Algorithms;

import com.shortesttour.utils.graph.GraphNode;

public class NearestNeighbor {

    public int[] createPath(GraphNode[][] graph){
        int[] path = new int[graph.length];
        boolean[] visited = createVisited(graph.length);

        visited[0] = true;
        path[0] = 0;

        int next = 0;
        for(int i=1;i<graph.length;i++){
            next = findMinDestination(graph[next],visited);
            path[i] = next;
            visited[next] = true;
        }

        path[graph.length-1] = 0;

        return path;
    }

    private int findMinDestination(GraphNode[] des,boolean[] visited){

        int minDesIdx = 0;
        for(int i = 0;i<des.length;i++){
            if(!visited[i]&&des[i].getDistance()!=-1)
                minDesIdx = i;
        }
        for(int i=0;i<des.length;i++){
            int dis = des[i].getDistance();
            int currentMin = des[minDesIdx].getDistance();
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
}
