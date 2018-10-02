package com.shortesttour.utils;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.db.DirectionApiResult;
import com.shortesttour.db.DirectionApiResultRepository;
import com.shortesttour.models.Place;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.observable.ObservableAll;
import io.reactivex.schedulers.Schedulers;

public class FindPathUtils {

    private long totalRuntime = 0;
    private long startRuntime;
    private long runtime;

    private GraphUtils graphUtils;

    private List<Place> mPlaceList;
    private Queue<Place> placeQueue;
    private TaskListener mListener;

    private int totalDistance;
    private int totalDuration;

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

        totalDistance = 0;
        totalDuration = 0;

        graphUtils.expandGraph(null);

        repository = new DirectionApiResultRepository(activity.getApplication());
      //  repository.deleteAll();
    }

    public void setOnTaskFinishListener(TaskListener listener) {
        mListener = listener;
    }

    public void findPath(final List<Place> placeList) {
        final int[] path = graphUtils.createPathNearest();
        final int pathLength = path.length;

        Observable.fromCallable(new Callable<JSONParserUtils.ParserData[]>() {
            @Override
            public JSONParserUtils.ParserData[] call(){
                Log.d("TEST", "call: ");
                String[] apiResponse = new String[mPlaceList.size()];
                for (int i = 0; i < pathLength; i++) {
                    int src = i<pathLength-1 ? path[i] : path[pathLength-1];
                    int des = i<pathLength-1 ? path[i+1] : 0;
                    DirectionApiResult apiResult = fetchFromDb(placeList.get(src).getPlaceId(),mPlaceList.get(des).getPlaceId()).blockingSingle();
                    Log.d("Test", "call2: " + apiResponse);
                    if(apiResult.getApiResult().contentEquals("")){
                        String url = getDirectionsUrl(placeList.get(src).getPlaceLatLng(), placeList.get(des).getPlaceLatLng());

                        apiResponse[i] = requestAPI(url).blockingSingle()[0];

                        DirectionApiResult directionApiResult = new DirectionApiResult();
                        directionApiResult.setSourceId(placeList.get(src).getPlaceId());
                        directionApiResult.setDestinationId(mPlaceList.get(des).getPlaceId());
                        directionApiResult.setApiResult(apiResponse[i]);

                        repository.insert(directionApiResult);
                    }else{
                        Log.d("DB", "DB: fetch data from DB : " + apiResult.getApiResult());
                        apiResponse[i] = apiResult.getApiResult();
                    }
                }
                JSONParserUtils.ParserData[] parserData = parseJSONData(apiResponse).blockingSingle();

                return parserData;
            }
        })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<JSONParserUtils.ParserData[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONParserUtils.ParserData[] parserData) {
                        try {

                            for(JSONParserUtils.ParserData data:parserData){
                                drawPath(data);
                            }

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
                    }
                });
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

                JSONParserUtils.ParserData[] parserData = parseJSONData(apiResponse).blockingSingle();

                List<Integer> distances = new ArrayList<>();

                for (int i = 0; i < parserData.length; i++) {
                    distances.add(parserData[i].getDistance());
                }

                publishProgress(75);
                graphUtils.expandGraph(distances);

                publishProgress(80);
                int[] path = graphUtils.createPathNearest();

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

                            graphUtils.showGraph();
                            graphUtils.showPath();

                            int[] nearestPathValue = getNearestPathValue();

                            System.out.println("--------Distances----------");
                            for (int i = 0; i < nearestPathValue.length; i++)
                                System.out.print(nearestPathValue[i] + " + ");

                            System.out.println("");
                            System.out.println("--------SUM Distances----------");
                            System.out.println("Sum Distance = " + getNearestSumDistance());


                            totalDistance = graphUtils.getNearestSumDistance();

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

                        runtime = System.currentTimeMillis() - startRuntime;
                        totalRuntime += runtime;

                        Log.d("Runtime", "Number of nodes = " + mPlaceList.size());
                        Log.d("Runtime", "Runtime = " + runtime +" ms");
                        Log.d("Runtime", "Runtime = " + runtime/1000 +" s");
                        Log.d("Runtime", "Total Runtime =  " + totalRuntime + " ms");
                        Log.d("Runtime", "Total Runtime =  " + totalRuntime/1000 + " s");
                    }
                });
    }

    private String[] getApiResponse(Place newPlace){

        String[] apiResponse = new String[mPlaceList.size()];

        for (int i = 0; i < apiResponse.length; i++) {
            DirectionApiResult apiResult = fetchFromDb(newPlace.getPlaceId(),mPlaceList.get(i).getPlaceId()).blockingSingle();
            if(apiResult.getApiResult().contentEquals("")){
                String url = getDirectionsUrl(newPlace.getPlaceLatLng(), mPlaceList.get(i).getPlaceLatLng());
                apiResponse[i] = requestAPI(url).blockingSingle()[0];
                DirectionApiResult directionApiResult = new DirectionApiResult();
                directionApiResult.setSourceId(newPlace.getPlaceId());
                directionApiResult.setDestinationId(mPlaceList.get(i).getPlaceId());
                directionApiResult.setApiResult(apiResponse[i]);
                repository.insert(directionApiResult);
            }else{
                apiResponse[i] = apiResult.getApiResult();
            }
        }

        return apiResponse;
    }

    public int[] getNearestPathValue() {
        return graphUtils.getNearestPathValue();
    }

    public int getNearestSumDistance() {
        return graphUtils.getNearestSumDistance();
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

    private Observable<DirectionApiResult> fetchFromDb(final int id1, final int id2){
        return Observable.fromCallable(new Callable<DirectionApiResult>() {
            @Override
            public DirectionApiResult call() throws Exception {
                DirectionApiResult apiResult = repository.getApiResult(id1,id2);
                if(apiResult == null){
                    apiResult = new DirectionApiResult();
                    apiResult.setSourceId(id1);
                    apiResult.setDestinationId(id2);
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

    private Observable<JSONParserUtils.ParserData[]> parseJSONData(final String[] response) {
        return Observable.fromCallable(new Callable<JSONParserUtils.ParserData[]>() {
            @Override
            public JSONParserUtils.ParserData[] call() throws Exception {
                JSONParserUtils.ParserData[] parserData = new JSONParserUtils.ParserData[response.length];
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

    private void drawPath(JSONParserUtils.ParserData result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.getRoutes().size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.getRoutes().get(i);

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
        Log.d("data", "onPostExecute: distance : " + result.getDistance() + ", duration : " + result.getDuration());
        if (mListener != null)
            mListener.onDrawPath(lineOptions);
    }

    public List<Place> collapseGraph(int position) {
        graphUtils.collapseGraph(position);
        mPlaceList.remove(position);
        return mPlaceList;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public int getTotalDuration() {
        return totalDuration;
    }
}