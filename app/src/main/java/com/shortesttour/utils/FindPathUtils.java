package com.shortesttour.utils;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.models.Place;

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
import java.util.Queue;
import java.util.concurrent.Callable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FindPathUtils {
    private GraphUtils graphUtils;

    private List<Place> mPlaceList;
    private Queue<Place> placeQueue;
    private TaskListener mListener;

    private int totalDistance;
    private int totalDuration;

    private boolean isTaskRunning = false;

    private static final int MAX_PROGRESS_GET_VALUE = 70;

    public interface TaskListener {
        void onFinishTask(int[] path);

        void OnStartTask(String placeName);

        void onUpdateValue(int value);

        void onDrawPath(PolylineOptions lineOptions);
    }

    public FindPathUtils(List<Place> placeList) {

        mPlaceList = placeList;
        graphUtils = new GraphUtils();
        placeQueue = new LinkedList<>();

        totalDistance = 0;
        totalDuration = 0;

        graphUtils.expandGraph(null);
    }

    public void setOnTaskFinishListener(TaskListener listener) {
        mListener = listener;
    }

    public void findPath(List<Place> placeList) {
        int[] path = graphUtils.createPathNearest();
        int pathLength = path.length;

        List<String> placesUrl = new ArrayList<>();
        for (int i = 0; i < pathLength - 1; i++) {
            placesUrl.add(getDirectionsUrl(placeList.get(path[i]).getPlaceLatLng(), placeList.get(path[i + 1]).getPlaceLatLng()));
        }
        placesUrl.add(getDirectionsUrl(placeList.get(path[pathLength - 1]).getPlaceLatLng(), placeList.get(path[0]).getPlaceLatLng()));

        for (String placeUrlString : placesUrl) {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(placeUrlString);
        }
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

        if (!isTaskRunning) {
            Log.d("test", "addPlace: connectNode");
            connectNodes();
        }
    }

    private void connectNodes() {
        isTaskRunning=true;
        final Place newPlace = placeQueue.remove();
        if (mListener != null)
            mListener.OnStartTask(newPlace.getPlaceTitle());
        Observable.fromCallable(new Callable<int[]>() {
            @Override
            public int[] call() throws Exception {
                LatLng newPlaceLatLng = newPlace.getPlaceLatLng();
                mPlaceList.add(newPlace);
                List<String> placesUrl = new ArrayList<>();
                for (int i = 0; i < mPlaceList.size(); i++) {
                    placesUrl.add(getDirectionsUrl(newPlaceLatLng, mPlaceList.get(i).getPlaceLatLng()));
                }
                String[] url = placesUrl.toArray(new String[placesUrl.size()]);
                String[] response = requestAPI(url).blockingSingle();
                JSONParserUtils.ParserData[] parserData = parseJSONData(response).blockingSingle();
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
                .subscribeOn(Schedulers.io())
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

                    }

                    @Override
                    public void onComplete() {
                        Log.d("response", "onComplete: ");
                        isTaskRunning = false;
                    }
                });
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

    private Observable<String[]> requestAPI(final String[] requestUrls) {
        return Observable.fromCallable(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] result = new String[requestUrls.length];

                int progressValue = 0;

                for (int i = 0; i < requestUrls.length; i++) {
                    do {
                        try {
                            result[i] = downloadUrl(requestUrls[i]);
                        } catch (Exception e) {
                            Log.e("error", "call: ", e);
                        }
                        Log.d("check", "data =  " + result[i]);
                    } while (result[i].contains("error") || result[i].contentEquals(""));
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

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            do {
                try {
                    data = downloadUrl(url[0]);
                } catch (Exception e) {
                    Log.d("Background Task", e.toString());
                    break;
                }
            } while (data.contains("error") || data.contentEquals(""));
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, JSONParserUtils.ParserData> {

        // Parsing the data in non-ui thread
        @Override
        protected JSONParserUtils.ParserData doInBackground(String... jsonData) {

            JSONObject jObject;
            JSONParserUtils.ParserData data = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONParserUtils parser = new JSONParserUtils();

                // Starts parsing data
                data = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(JSONParserUtils.ParserData result) {
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