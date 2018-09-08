package com.shortesttour.utils;
import android.util.Log;

import java.util.List;

public class GraphUtils {

    private int[][] graph;
    private int[][] tempGraph;
    private boolean[] visited;

    public GraphUtils(){
        graph = new int[0][0];
        tempGraph = new int[0][0];
        visited = new boolean[0];
    }

    public void setGraph(int[][] graph){
        this.graph = graph;
    }

    public void connectEdge(int row,int col,int value){
        graph[row][col] = value;
        graph[col][row] = value;
    }

    public void setValue(int row,int col,int value){
        graph[row][col] = value;
    }

    public int getValue(int row,int col){
        return graph[row][col];
    }

    public int findMinIndexCol(int col){
        int minIndex = 0;
        int[] colArray = graph[col];
        for(int i=0;i<colArray.length;i++){
            if(colArray[i]!=0&&colArray[i]<=colArray[minIndex])
                minIndex = i;
        }
        return minIndex;
    }

    public int findMaxIndexCol(int col){
        int maxIndex = 0;
        int[] colArray = graph[col];
        for(int i=0;i<colArray.length;i++){
            if(colArray[i]!=0&&colArray[i]>=colArray[maxIndex])
                maxIndex = i;
        }
        return maxIndex;
    }

    public int findMinIndexRow(int row){
        int minIndex = 0;
        int[] rowArray = graph[row];
        for(int i=0;i<rowArray.length;i++){
            if(rowArray[i]!=0&&rowArray[i]<=rowArray[minIndex])
                minIndex = i;
        }
        return minIndex;
    }

    public int findMaxIndexRow(int row){
        int maxIndex = 0;
        int[] rowArray = graph[row];
        for(int i=0;i<rowArray.length;i++){
            if(rowArray[i]!=0&&rowArray[i]<=rowArray[maxIndex])
                maxIndex = i;
        }
        return maxIndex;
    }

    public int getDimen(){
        return graph.length;
    }

    public void resetVisit(){
        for(int i=0;i<visited.length;i++)
            visited[i] = false;
    }

    //Nearest Neightbor
    private void cloneGraph(int[][] dest,int[][] src){
        int len = dest.length;
        for(int i=0;i<len;i++){
            for(int j=0;j<len;j++){
                if(i<src.length&&j<src.length)
                    dest[i][j] = src[i][j];
                else
                    dest[i][j] = 0;
            }
        }
    }

    public void expandGraph(List<Integer> values){
        int len = getDimen();
        tempGraph = new int[len][len];
        cloneGraph(tempGraph,graph);

        graph = new int[len+1][len+1];
        cloneGraph(graph,tempGraph);

        for(int i=0;i<len;i++)
            connectEdge(i,len,values.get(i));

        visited = new boolean[len+1];
        resetVisit();

        showGraph();
        showPath();
    }

    private int findMinDestination(int row){
        int minDesIdx=0;
        int[] destinations = graph[row];
        for(int i =0;i<destinations.length;i++){
            if(!visited[i]&&destinations[i]!=0)
                minDesIdx = i;
        }
        for(int i=0;i<destinations.length;i++){
            if(!visited[i]&&destinations[i]!=0&&destinations[i]<=destinations[minDesIdx])
                minDesIdx = i;
        }
        return minDesIdx;
    }

    public int[] createPathNearest(){
        int[] path = new int[getDimen()];
        resetVisit();

        visited[0] = true;
        path[0] = 0;
        int nextDestination = 0;
        for(int i=1;i<getDimen();i++){
            nextDestination = findMinDestination(nextDestination);
            path[i] = nextDestination;
            visited[nextDestination] = true;
        }

        return path;
    }

    public int getNearestSumDistance(){
        int[] nearestDistance = getNearestPathValue();
        int sum = 0;
        for(int i=0;i<nearestDistance.length;i++)
            sum+=nearestDistance[i];
        return sum;
    }

    public int[] getNearestPathValue(){
        int[] path = createPathNearest();
        int[] distances = new int[path.length];
        for(int i=0;i<path.length-1;i++){
            distances[i] = graph[path[i]][path[i+1]];
        }
        distances[path.length-1] = graph[path[path.length-1]][path[0]];
        return distances;
    }

    public void showPath(){
        System.out.println("------------SHOW PATH-------------");
        int[] path = createPathNearest();

        if(path.length>1){
            for(int i=0;i<path.length;i++)
                System.out.print(path[i] + " -> ");
        }

        System.out.println("0|");
    }

    public void showGraph(){
        System.out.println("-------------SHOW GRAPH------------");
        for(int i=0;i<getDimen();i++){
            for(int j=0;j<getDimen();j++){
                System.out.printf("%d ",graph[i][j]/1000);
            }
            System.out.printf("\n");
        }
    }

}