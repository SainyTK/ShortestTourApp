package com.shortesttour.utils.graph;

import android.content.Context;
import android.util.Log;

import com.shortesttour.utils.PrefsUtil;
import com.shortesttour.utils.graph.Algorithms.DynamicProgramming;
import com.shortesttour.utils.graph.Algorithms.NearestNeighbor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GraphUtils{

    private GraphNode[][] graph;
    private int[] path;

    private NearestNeighbor nearestNeighbor;
    private DynamicProgramming dynamicProgramming;

    private Context context;

    public GraphUtils(Context context) {
        this.context = context;
        graph = new GraphNode[0][0];
        path = new int[0];

        nearestNeighbor = new NearestNeighbor() {
            @Override
            public void onProgress(int value) {
                setProgress(value);
            }
        };

        dynamicProgramming = new DynamicProgramming(){
            @Override
            public void onProgress(int value) {
                setProgress(value);
            }
        };
    }

    public abstract void setProgress(int val);

    public void connectEdge(int row, int col, int distance, int duration, List<List<HashMap<String, String>>> route) {
        graph[row][col].setDistance(distance);
        graph[col][row].setDistance(distance);

        graph[row][col].setDuration(duration);
        graph[col][row].setDuration(duration);

        graph[row][col].setRoutes(route);
        graph[col][row].setRoutes(route);
    }

    public int getDimen() {
        return graph.length;
    }

    private void cloneGraph(GraphNode[][] dest, GraphNode[][] src) {
        int len = dest.length;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i < src.length && j < src.length)
                    dest[i][j] = src[i][j];
                else
                    dest[i][j] = new GraphNode();
            }
        }
    }

    public synchronized void expandGraph(GraphNode[] nodes) {
        int len = getDimen();
        GraphNode[][] tempGraph = new GraphNode[len][len];
        cloneGraph(tempGraph, graph);

        graph = new GraphNode[len + 1][len + 1];
        cloneGraph(graph, tempGraph);

        for (int i = 0; i < len; i++) {
            GraphNode node = nodes[i];
            connectEdge(i, len, node.getDistance(), node.getDuration(), node.getRoutes());
        }

        calculatePath();
    }

    public synchronized void collapseGraph(int position) {
        int len = getDimen();
        GraphNode[][] tempGraph = new GraphNode[len - 1][len - 1];

        for (int i = 0, k = 0; i < len; i++) {
            for (int j = 0, l = 0; j < len; j++) {
                if (i != position && j != position) {
                    tempGraph[k][l] = graph[i][j];
                    l++;
                }
            }
            if (i != position)
                k++;
        }
        graph = tempGraph;

        calculatePath();
    }

    public void calculatePath() {
        int algorithm = PrefsUtil.getAlgorithm(context);
        int[] path = new int[0];
        switch (algorithm) {
            case PrefsUtil.NEAREST_NEIGHBOR:
                path = nearestNeighbor.createPath(getDistanceGraph());
                break;
            case PrefsUtil.DYNAMIC_PROGRAMMING:
                path = dynamicProgramming.createPath(getDistanceGraph());
                break;
        }
        if(path.length>2)
            this.path = path;
        else
            this.path = new int[0];

        Log.d("SHOW GRAPH", "calculatePath: " + this);
    }

    public int[] getNearestPathDuration() {
        int durations[] = new int[path.length];
        for (int i = 0; i < path.length - 1; i++) {
            durations[i] = graph[path[i]][path[i + 1]].getDuration();
        }
        return durations;
    }

    public int getNearestSumDuration() {
        int[] nearestPathDuration = getNearestPathDuration();
        int sum = 0;
        for (int i = 0; i < nearestPathDuration.length; i++)
            sum += nearestPathDuration[i];
        return sum;
    }

    public int[] getNearestPathDistance() {
        int[] distances = new int[path.length];
        for (int i = 0; i < path.length - 1; i++) {
            distances[i] = graph[path[i]][path[i + 1]].getDistance();
        }

        return distances;
    }

    public int getNearestSumDistance() {
        int[] nearestDistance = getNearestPathDistance();
        int sum = 0;
        for (int i = 0; i < nearestDistance.length; i++)
            sum += nearestDistance[i];
        return sum;
    }

    public List<List<List<HashMap<String, String>>>> getRoutes() {
        List<List<List<HashMap<String, String>>>> nearestRoutes = new ArrayList<>();
        for (int i = 0; i < path.length - 1; i++) {
            nearestRoutes.add(graph[path[i]][path[i + 1]].getRoutes());
        }
        return nearestRoutes;
    }

    public int[][] getDistanceGraph() {
        int[][] distanceGraph = new int[getDimen()][getDimen()];
        for (int i = 0; i < getDimen(); i++) {
            for (int j = 0; j < getDimen(); j++) {
                distanceGraph[i][j] = graph[i][j].getDistance();
            }
        }
        return distanceGraph;
    }

    public int[] getPath() {
        return path;
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

    public String pathText() {
        StringBuilder sb = new StringBuilder();
        sb.append("------------SHOW PATH-------------\n");

        if (path.length > 1) {
            for (int p : path)
                sb.append(p + " -> ");
        }

        sb.append("\n");
        return sb.toString();
    }

    public String distanceGraphText() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------SHOW DISTANCE GRAPH (KM)-----------\n");
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                sb.append(graph[i][j].getDistance() / 1000 + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String durationGraphText() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------SHOW DURATION GRAPH (MIN)-----------\n");
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                sb.append(graph[i][j].getDuration() / 60 + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String sumDistanceText() {
        StringBuilder sb = new StringBuilder();
        sb.append("SUM DISTANCE = ");
        sb.append(getNearestSumDistance());
        sb.append("\n");
        return sb.toString();
    }

    public String sumDurationText() {
        StringBuilder sb = new StringBuilder();
        sb.append("SUM DURATION = ");
        sb.append(getNearestSumDuration()/60);
        sb.append("\n");
        return sb.toString();
    }
}