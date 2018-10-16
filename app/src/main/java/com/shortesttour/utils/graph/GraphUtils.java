package com.shortesttour.utils.graph;

import android.util.Pair;

import com.shortesttour.utils.graph.Algorithms.NearestNeighbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphUtils {

    private GraphNode[][] graph;
    private List<Pair<Integer,GraphNode>> path;

    private NearestNeighbor nearestNeighbor;

    public GraphUtils(){
        graph = new GraphNode[0][0];
        path = new ArrayList<>();
        nearestNeighbor = new NearestNeighbor();
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

    public void updateGraph(int[] path){
        GraphNode[][] newGraph = new GraphNode[getDimen()][getDimen()];

        for(int i=0;i<getDimen();i++){
            for(int j=0;j<getDimen();j++){
                newGraph[i][j] = graph[path[j]][path[i]];
            }
        }

        this.graph = newGraph;
    }

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

            setPath(nearestNeighbor.createPath(graph));
    }

    public synchronized void collapseGraph(int position){
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

        setPath(nearestNeighbor.createPath(graph));
    }

    private void setPath(int[] newPath){
        for(int i=0;i<newPath.length-1;i++){
            GraphNode node = graph[newPath[i]][newPath[i+1]];
            path.set(i,new Pair<>(newPath[i],node));
        }
    }

    public int[] getNearestPathDuration(){
        int durations[] = new int[path.size()];
        for(int i=0;i < path.size();i++){
            durations[i] = path.get(i).second.getDuration();
        }
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
        int[] distances = new int[path.size()];
        for(int i=0;i<path.size();i++){
            distances[i] = path.get(i).second.getDistance();
        }
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
        for(int i=0;i<path.size();i++) {
            nearestRoutes.add(path.get(i).second.getRoutes());
        }
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

        if(path.size()>1){
            for(Pair<Integer,GraphNode> p : path)
                sb.append(p.first + " -> ");
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