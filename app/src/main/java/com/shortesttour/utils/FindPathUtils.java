package com.shortesttour.utils;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.db.DirectionApiResult;
import com.shortesttour.db.DirectionApiResultRepository;
import com.shortesttour.models.Place;
import com.shortesttour.utils.graph.GraphNode;
import com.shortesttour.utils.graph.GraphUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.Callable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FindPathUtils {

    private long totalRuntime = 0;
    private long startRuntime = 0;
    private ArrayList<Long> runtimes = new ArrayList<>();


    private GraphUtils graphUtils;

    private List<Place> mPlaceList;
    private Queue<Place> placeQueue;
    private TaskListener mListener;

    DirectionApiResultRepository repository;

    private boolean isTaskRunning = false;

    private static final int MAX_PROGRESS_GET_VALUE = 70;

    public interface TaskListener {
        void onFinishTask(int[] path);

        void OnStartTask(String placeName);

        void onUpdateValue(int value);

        void onDrawPath(PolylineOptions lineOptions);
    }

    public FindPathUtils(AppCompatActivity activity,List<Place> placeList) {

        mPlaceList = placeList;
        graphUtils = new GraphUtils();
        placeQueue = new LinkedList<>();

        graphUtils.expandGraph(null);

        repository = new DirectionApiResultRepository(activity.getApplication());
        //repository.deleteAll();
    }

    public void setOnTaskFinishListener(TaskListener listener) {
        mListener = listener;
    }

    public void findPath() {
//        List<List<HashMap<String,String>>>[] route = graphUtils.getRoutes().toArray(new List[graphUtils.getRoutes().size()]);

//        for(List<List<HashMap<String,String>>> path:graphUtils.getRoutes()){
//            drawPath(path);
//        }
    }

    private void updatePlaceList(int[] path) {
        List<Place> sortedPlace = new ArrayList<>();
        int pathLength = path.length;

        for (int i = 0; i < pathLength; i++) {
            sortedPlace.add(mPlaceList.get(path[i]));
        }
        mPlaceList = sortedPlace;
    }

    public List<Place> getPlaceList() {
        return mPlaceList;
    }

    public void addPlace(Place newPlace) {
        placeQueue.add(newPlace);
        Log.d("Test", "Enqueue Queue Size: " + placeQueue.size());

        if (!isTaskRunning) {
            Log.d("test", "addPlace: connectNode");
            connectNodes();
        }
    }

    synchronized private void connectNodes() {

        startRuntime = System.currentTimeMillis();

        isTaskRunning = true;
        final Place newPlace = placeQueue.remove();
        Log.d("Test", "Dequeue Queue Size: " + placeQueue.size());
        if (mListener != null)
            mListener.OnStartTask(newPlace.getPlaceTitle());

        Observable.fromCallable(new Callable<int[]>() {
            @Override
            public int[] call(){
                Log.d("TEST", "call: ");

                String[] apiResponse = getApiResponse(newPlace);

                mPlaceList.add(newPlace);

                GraphNode[] nodes = parseJSONData(apiResponse).blockingSingle();

                publishProgress(75);
                graphUtils.expandGraph(nodes);

                publishProgress(80);
                int[] path = graphUtils.createNearestPath();

                updatePlaceList(path);
                graphUtils.updateGraph(path);
                return path;
            }
        })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<int[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(int[] path) {
                        try {

                            publishProgress(90);

                            Log.d("test", "Show Graph : " + graphUtils);

                            publishProgress(100);

                            if (placeQueue.size() > 0)
                                connectNodes();
                            if (mListener != null)
                                mListener.onFinishTask(path);
                        } catch (Exception e) {
                            Log.e("error", "onPostExecute: ", e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d("response", "onComplete: ");
                        isTaskRunning = false;

                        long finishTime = System.currentTimeMillis();
                        long runtime = finishTime - startRuntime;
                        totalRuntime += runtime;
                        runtimes.add(runtime);

                        String s = "";

                        Log.d("Runtime", "Number of nodes : " + mPlaceList.size());
                        Log.d("Runtime", "Runtime : ");
                        for(Long time: runtimes){
                            s += time.toString() + " ";
                        }
                        Log.d("Runtime", s);

                    }
                });
    }

    private String[] getApiResponse(Place newPlace){

        String[] apiResponse = new String[mPlaceList.size()];

        for (int i = 0; i < apiResponse.length; i++) {
            DirectionApiResult apiResult = fetchFromDb(newPlace.getLatitude(),newPlace.getLongitude(),
                    mPlaceList.get(i).getLatitude(),mPlaceList.get(i).getLongitude()).blockingSingle();
            if(apiResult.getApiResult().contentEquals("")){
                String url = getDirectionsUrl(newPlace.getPlaceLatLng(), mPlaceList.get(i).getPlaceLatLng());
                apiResponse[i] = requestAPI(url).blockingSingle()[0];
                DirectionApiResult directionApiResult = new DirectionApiResult();

                directionApiResult.setSrcLat(newPlace.getLatitude());
                directionApiResult.setSrcLng(newPlace.getLongitude());
                directionApiResult.setDesLat(mPlaceList.get(i).getLatitude());
                directionApiResult.setDesLng(mPlaceList.get(i).getLongitude());

                directionApiResult.setApiResult(apiResponse[i]);
                repository.insert(directionApiResult);
            }else{
                apiResponse[i] = apiResult.getApiResult();
            }
        }

        return apiResponse;
    }

    public int[] getNearestDistance() {
        return graphUtils.getNearestPathDistance();
    }

    public int getNearestSumDistance() {
        return graphUtils.getNearestSumDistance();
    }

    public int[] getNearestDuration() {
        return graphUtils.getNearestPathDuration();
    }

    public int getNearestSumDuration() {
        return graphUtils.getNearestSumDuration();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        String key = "AIzaSyBCGQy_slw6UDK7NGzcamjxHc7J_LLixW8";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + key;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

            iStream.close();
            urlConnection.disconnect();

        } catch (Exception e) {
            Log.d("Exception ", e.toString());
        }
        return data;
    }

    private Observable<DirectionApiResult> fetchFromDb(final double srcLat, final double srcLng,final double desLat,final double desLng){
        return Observable.fromCallable(new Callable<DirectionApiResult>() {
            @Override
            public DirectionApiResult call() throws Exception {
                DirectionApiResult apiResult = repository.getApiResult(srcLat,srcLng,desLat,desLng);
                if(apiResult == null){
                    apiResult = new DirectionApiResult();
                    apiResult.setSrcLat(srcLat);
                    apiResult.setSrcLng(srcLng);
                    apiResult.setDesLat(desLat);
                    apiResult.setDesLng(desLng);
                    apiResult.setApiResult("");
                }
                return apiResult;
            }
        });
    }

    private Observable<String[]> requestAPI(final String... requestUrls) {
        return Observable.fromCallable(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] result = new String[requestUrls.length];

                int progressValue = 0;
                boolean checkError = false;
                int numError = 0;

                for (int i = 0; i < requestUrls.length; i++) {
                    do {
                        try {
                            Log.d("url", "request url : " + requestUrls[i]);
                            result[i] = downloadUrl(requestUrls[i]);
                            Log.d("url", "response : " + result[i]);
                            checkError = result[i].contains("error") || result[i].contentEquals("");
                            if(checkError){
                                Thread.sleep(1000);
                                numError++;
                            }
                        } catch (Exception e) {
                            Log.e("error", "call: ", e);
                        }
                        Log.d("check", "data =  " + result[i]);
                    } while (checkError);
                    progressValue = (i + 1) * MAX_PROGRESS_GET_VALUE / requestUrls.length;
                    publishProgress(progressValue);
                }
                return result;
            }
        });
    }

    private Observable<GraphNode[]> parseJSONData(final String[] response) {
        return Observable.fromCallable(new Callable<GraphNode[]>() {
            @Override
            public GraphNode[] call() throws Exception {
                GraphNode[] parserData = new GraphNode[response.length];
                JSONObject jsonObject;

                for (int i = 0; i < response.length; i++) {
                    try {
                        JSONParserUtils jsonParserUtils = new JSONParserUtils();
                        jsonObject = new JSONObject(response[i]);

                        parserData[i] = jsonParserUtils.parse(jsonObject);

                    } catch (JSONException e) {
                        Log.e("error", "doInBackground: ", e);
                    }
                    publishProgress((i + 1) * 10 / response.length + MAX_PROGRESS_GET_VALUE);
                }
                return parserData;
            }
        });
    }

    private void publishProgress(int value) {
        if (mListener != null)
            mListener.onUpdateValue(value);
    }

    private void drawPath(List<List<HashMap<String, String>>> paths) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < paths.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = paths.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(2);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        if (mListener != null)
            mListener.onDrawPath(lineOptions);
    }

    public List<Place> collapseGraph(int position) {
        graphUtils.collapseGraph(position);
        mPlaceList.remove(position);
        return mPlaceList;
    }

}