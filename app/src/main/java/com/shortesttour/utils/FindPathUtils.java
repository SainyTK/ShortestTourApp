package com.shortesttour.utils;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.models.Place;
import com.shortesttour.ui.main.MainActivity;

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

public class FindPathUtils {
    private GoogleMap mMap;
    private GraphUtils graphUtils;

    private Queue<String[]> urlQueue;
    private OnTaskFinishListener mListener;

    public interface OnTaskFinishListener{
        void onGetValue();
        void onDrawPath();
    }

    public FindPathUtils(){
        graphUtils = new GraphUtils();
        urlQueue = new LinkedList<>();

        graphUtils.expandGraph(null);
    }

    public void setOnTaskFinishListener(OnTaskFinishListener listener){
        mListener = listener;
    }

    public void findPath(GoogleMap map, List<Place> placeList) {
        mMap = map;

        List<String> placesUrl = new ArrayList<>();
        for (int i = 0; i < placeList.size()-1; i++) {
            placesUrl.add(getDirectionsUrl(placeList.get(i).getPlaceLatLng(), placeList.get(i+1).getPlaceLatLng()));
        }
        placesUrl.add(getDirectionsUrl(placeList.get(placeList.size()-1).getPlaceLatLng(), placeList.get(0).getPlaceLatLng()));

        for (String placeUrlString : placesUrl) {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(placeUrlString);
        }
    }

    public List<Place> sortNearest(List<Place> placeList){
        List<Place> sortedPlace = new ArrayList<>();
        int[] path = graphUtils.createPathNearest();
        int pathLength = path.length;

        for (int i = 1; i < pathLength; i++) {
            sortedPlace.add(placeList.get(path[i]));
        }
        return sortedPlace;
    }

    public void addPlace(GoogleMap map, List<Place> placeList, Place newPlace) {
        mMap = map;

        LatLng newPlaceLatLng = newPlace.getPlaceLatLng();

        List<String> placesUrl = new ArrayList<>();
        for (int i = 0; i < placeList.size(); i++) {
            placesUrl.add(getDirectionsUrl(newPlaceLatLng, placeList.get(i).getPlaceLatLng()));
        }

        String[] placesUrlArr = placesUrl.toArray(new String[placesUrl.size()]);

        urlQueue.add(placesUrlArr);
        if(urlQueue.size()==1)
            connectNodes();
    }

    public void connectNodes(){
        String[] url = urlQueue.remove();
        GetValueTask getValueTask = new GetValueTask();
        getValueTask.execute(url);
    }

    public int[] getNearestPathValue(){
        return graphUtils.getNearestPathValue();
    }

    public int getNeatestSumDistance(){
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

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

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

        } catch (Exception e) {
            Log.d("Exception ", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class GetValueTask extends AsyncTask<String,Integer,String[]>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String[] doInBackground(String... strings) {
            String[] result = new String[strings.length];
            for(int i=0;i<strings.length;i++){
                do{
                    try{
                        result[i] = downloadUrl(strings[i]);
                    }catch (Exception e){
                        Log.e("error", "doInBackground: ", e);
                    }
                }while (result[i].contains("error"));
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

    private class UpdateValueTask extends AsyncTask<String,Integer,JSONParserUtils.ParserData[]>{

        @Override
        protected JSONParserUtils.ParserData[] doInBackground(String... strings) {
            JSONParserUtils.ParserData[] parserData = new JSONParserUtils.ParserData[strings.length];
            JSONObject jsonObject;

            for(int i=0;i<strings.length;i++){
                try{
                    JSONParserUtils jsonParserUtils = new JSONParserUtils();
                    jsonObject = new JSONObject(strings[i]);

                    parserData[i] = jsonParserUtils.parse(jsonObject);


                }catch (JSONException e){
                    Log.e("error", "doInBackground: ",e );
                }
            }
            return parserData;
        }

        @Override
        protected void onPostExecute(JSONParserUtils.ParserData[] parserData) {
            super.onPostExecute(parserData);

            List<Integer> distances = new ArrayList<>();

            try{
                for(int i=0;i<parserData.length;i++){
                    distances.add(parserData[i].getDistance());
                }

                graphUtils.expandGraph(distances);

                int[] nearestPathValue = graphUtils.getNearestPathValue();

                System.out.println("--------Distances----------");
                for(int i=0;i<nearestPathValue.length;i++)
                    System.out.print(nearestPathValue[i] + " + ");

                System.out.println("");
                System.out.println("--------SUM Distances----------");
                System.out.println("Sum Distance = " + graphUtils.getNearestSumDistance());

                if(mListener!=null)
                    mListener.onGetValue();

                if(urlQueue.size()>0)
                    connectNodes();
            }catch (Exception e){
                Log.e("error", "onPostExecute: ",e );
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

    private class ParserTask extends AsyncTask<String, Integer, JSONParserUtils.ParserData>{

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
            MarkerOptions markerOptions = new MarkerOptions();

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
            mMap.addPolyline(lineOptions);
        }
    }
}
