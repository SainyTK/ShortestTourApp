package com.shortesttour.utils;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.models.Place;
import com.shortesttour.ui.main.MainActivity;
import com.shortesttour.utils.Room.DirectionApiResult;
import com.shortesttour.utils.Room.DirectionApiResultViewModel;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class FindPathUtils {
    private GraphUtils graphUtils;

    private GetValueTask task;
    private List<Place> mPlaceList;
    private Queue<Place> placeQueue;
    private TaskListener mListener;

    private String newPlaceTitle;

    private int totalDistance;
    private int totalDuration;

    private Activity activity;
    private DirectionApiResultViewModel viewModel;

    private static final int MAX_PROGRESS_GET_VALUE = 70;

    public interface TaskListener {
        void onFinishTask(String placeName);

        void OnStartTask(String placeName);

        void onUpdateValue(int value);

        void onDrawPath(PolylineOptions lineOptions);
    }

    public FindPathUtils(Activity activity, List<Place> placeList) {
        this.activity = activity;

        mPlaceList = placeList;
        graphUtils = new GraphUtils();
        placeQueue = new LinkedList<>();
        task = new GetValueTask();


        totalDistance = 0;
        totalDuration = 0;

        graphUtils.expandGraph(null);

        viewModel = ViewModelProviders.of((MainActivity) activity).get(DirectionApiResultViewModel.class);
        //viewModel.deleteAll();

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

        if (task.getStatus() != AsyncTask.Status.RUNNING)
            connectNodes();
    }

    private void connectNodes() {
        Place newPlace = placeQueue.remove();
        LatLng newPlaceLatLng = newPlace.getPlaceLatLng();




        newPlaceTitle = newPlace.getPlaceTitle();

        if (mListener != null)
            mListener.OnStartTask(newPlaceTitle);

        mPlaceList.add(newPlace);

        List<String> placesUrl = new ArrayList<>();
        for (int i = 0; i < mPlaceList.size(); i++) {
            placesUrl.add(getDirectionsUrl(newPlaceLatLng, mPlaceList.get(i).getPlaceLatLng()));
        }

        String[] url = placesUrl.toArray(new String[placesUrl.size()]);

        task = new GetValueTask();
        task.execute(url);
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

    private class GetValueTask extends AsyncTask<String, Integer, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            publishProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mListener != null)
                mListener.onUpdateValue(values[0]);
        }

        @Override
        protected String[] doInBackground(String... strings) {
            String[] result = new String[strings.length];

            int progressValue = 0;

            for (int i = 0; i < strings.length; i++) {
                do {
                    try {
                        result[i] = downloadUrl(strings[i]);
                    } catch (Exception e) {
                        Log.e("error", "doInBackground: ", e);
                    }
                    Log.d("check", "data =  " + result[i]);
                } while (result[i].contains("error") || result[i].contentEquals(""));
                progressValue = (i + 1) * MAX_PROGRESS_GET_VALUE / strings.length;
                publishProgress(progressValue);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            UpdateValueTask task = new UpdateValueTask();
            task.execute(strings);
        }
    }

    private class UpdateValueTask extends AsyncTask<String, Integer, JSONParserUtils.ParserData[]> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mListener != null)
                mListener.onUpdateValue(values[0]);
        }

        @Override
        protected JSONParserUtils.ParserData[] doInBackground(String... strings) {
            JSONParserUtils.ParserData[] parserData = new JSONParserUtils.ParserData[strings.length];
            JSONObject jsonObject;

            for (int i = 0; i < strings.length; i++) {
                try {
                    JSONParserUtils jsonParserUtils = new JSONParserUtils();
                    jsonObject = new JSONObject(strings[i]);

                    parserData[i] = jsonParserUtils.parse(jsonObject);

                } catch (JSONException e) {
                    Log.e("error", "doInBackground: ", e);
                }
                publishProgress((i + 1) * 10 / strings.length + MAX_PROGRESS_GET_VALUE);
            }
            return parserData;
        }

        @Override
        protected void onPostExecute(JSONParserUtils.ParserData[] parserData) {
            super.onPostExecute(parserData);

            List<Integer> distances = new ArrayList<>();

            try {
                for (int i = 0; i < parserData.length; i++) {
                    distances.add(parserData[i].getDistance());
                }
                publishProgress(75);

                graphUtils.expandGraph(distances);

                publishProgress(80);

                int[] path = graphUtils.createPathNearest();

                updatePlaceList(path);
                graphUtils.updateGraph(path);

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

                if (mListener != null)
                    mListener.onFinishTask(newPlaceTitle);

                totalDistance = graphUtils.getNearestSumDistance();

                publishProgress(100);

                if (placeQueue.size() > 0)
                    connectNodes();
            } catch (Exception e) {
                Log.e("error", "onPostExecute: ", e);
            }
        }
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