package com.shortesttour.utils.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphUtils {

    private GraphNode[][] graph;
    private boolean[] visited;

    public GraphUtils(){
        graph = new GraphNode[0][0];
        visited = new boolean[0];
    }

    public void connectEdge(int row,int col,int distance,int duration,List<List<HashMap<String, String>>> route){
        graph[row][col].setDistance(distance);
        graph[col][row].setDistance(distance);

        graph[row][col].setDuration(duration);
        graph[col][row].setDuration(duration);

        graph[row][col].setRoutes(route);
        graph[col][row].setRoutes(route);
    }

    public int getDimen(){
        return graph.length;
    }

    public void resetVisit(){
        for(int i=0;i<visited.length;i++)
            visited[i] = false;
    }

    public void updateGraph(int[] path){
        GraphNode[][] newGraph = new GraphNode[getDimen()][getDimen()];

        for(int i=0;i<getDimen();i++){
            for(int j=0;j<getDimen();j++){
                newGraph[i][j] = graph[path[j]][path[i]];
            }
        }

        graph = newGraph;
    }

    //Nearest Neightbor
    private void cloneGraph(GraphNode[][] dest,GraphNode[][] src){
        int len = dest.length;
        for(int i=0;i<len;i++){
            for(int j=0;j<len;j++){
                if(i<src.length&&j<src.length)
                    dest[i][j] = src[i][j];
                else
                    dest[i][j] = new GraphNode();
            }
        }
    }

    public synchronized void expandGraph(GraphNode[] nodes){
            int len = getDimen();
            GraphNode[][] tempGraph = new GraphNode[len][len];
            cloneGraph(tempGraph,graph);

            graph = new GraphNode[len+1][len+1];
            cloneGraph(graph,tempGraph);

            for(int i=0;i<len;i++){
                GraphNode node = nodes[i];
                connectEdge(i,len,node.getDistance(),node.getDuration(),node.getRoutes());
            }

            visited = new boolean[len+1];
            resetVisit();
    }

    public void collapseGraph(int position){
        int len = getDimen();
        GraphNode[][] tempGraph = new GraphNode[len-1][len-1];

        for(int i=0,k=0;i<len;i++){
            for(int j=0,l=0;j<len;j++){
                if(i!=position&&j!=position){
                    tempGraph[k][l] = graph[i][j];
                    l++;
                }
            }
            if(i!=position)
                k++;
        }

        graph = tempGraph;
    }

    private int findMinDestination(int row){
        int minDesIdx = 0;
        GraphNode[] destinations = graph[row];
        for(int i = 0;i<destinations.length;i++){
            if(!visited[i]&&destinations[i].getDistance()!=0)
                minDesIdx = i;
        }
        for(int i=0;i<destinations.length;i++){
            int dis = destinations[i].getDistance();
            int currentMin = destinations[minDesIdx].getDistance();
            if(!visited[i]&&dis!=0 && dis<=currentMin)
                minDesIdx = i;
        }
        return minDesIdx;
    }

    public int[] createNearestPath(){
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

    public int[] getNearestPathDuration(){
        int[] path = createNearestPath();
        int[] durations = new int[path.length];
        for(int i=0;i<path.length-1;i++){
            durations[i] = graph[path[i]][path[i+1]].getDuration();
        }
        durations[path.length-1] = graph[path[path.length-1]][path[0]].getDuration();
        return durations;
    }

    public int getNearestSumDuration(){
        int[] nearestPathDuration = getNearestPathDuration();
        int sum = 0;
        for(int i=0;i<nearestPathDuration.length;i++)
            sum+=nearestPathDuration[i];
        return sum;
    }


    public int[] getNearestPathDistance(){
        int[] path = createNearestPath();
        int[] distances = new int[path.length];
        for(int i=0;i<path.length-1;i++){
            distances[i] = graph[path[i]][path[i+1]].getDistance();
        }
        distances[path.length-1] = graph[path[path.length-1]][path[0]].getDistance();
        return distances;
    }

    public int getNearestSumDistance(){
        int[] nearestDistance = getNearestPathDistance();
        int sum = 0;
        for(int i=0;i<nearestDistance.length;i++)
            sum+=nearestDistance[i];
        return sum;
    }

    public List<List<List<HashMap<String,String>>>> getRoutes(){
        List<List<List<HashMap<String,String>>>> nearestRoutes = new ArrayList<>();
        int[] path = createNearestPath();
        for(int i=0;i<path.length-1;i++){
            nearestRoutes.add(graph[path[i]][path[i+1]].getRoutes());
        }
        nearestRoutes.add(graph[path[path.length-1]][path[0]].getRoutes());
        return nearestRoutes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pathText());
        sb.append(distanceGraphText());
        sb.append(durationGraphText());
        sb.append(sumDistanceText());
        sb.append(sumDurationText());
        return sb.toString();
    }

    public String pathText(){
        StringBuilder sb = new StringBuilder();
        sb.append("------------SHOW PATH-------------\n");
        int[] path = createNearestPath();

        if(path.length>1){
            for(Integer p : path)
                sb.append(p + " -> ");
        }

        sb.append("0|\n");
        return sb.toString();
    }

    public String distanceGraphText(){
        StringBuilder sb = new StringBuilder();
        sb.append("-------------SHOW DISTANCE GRAPH (KM)-----------\n");
        for(int i=0;i<graph.length;i++){
            for(int j=0;j<graph.length;j++){
                sb.append(graph[i][j].getDistance()/1000 + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String durationGraphText(){
        StringBuilder sb = new StringBuilder();
        sb.append("-------------SHOW DURATION GRAPH (MIN)-----------\n");
        for(int i=0;i<graph.length;i++){
            for(int j=0;j<graph.length;j++){
                sb.append(graph[i][j].getDuration()/60 + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String sumDistanceText(){
        StringBuilder sb = new StringBuilder();
        sb.append("SUM DISTANCE = ");
        sb.append(getNearestSumDistance());
        sb.append("\n");
        return sb.toString();
    }

    public String sumDurationText(){
        StringBuilder sb = new StringBuilder();
        sb.append("SUM DURATION = ");
        sb.append(getNearestSumDuration());
        sb.append("\n");
        return sb.toString();
    }

}