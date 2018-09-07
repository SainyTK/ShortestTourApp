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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindPathUtils {

    private List<Place> placeList;


    private GoogleMap mMap;

    private Handler handler;
    private Runnable runnable;

    public void findPath(GoogleMap map, List<Place> placeList) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() { }
        };

        mMap = map;
        this.placeList = placeList;

        List<String> placesUrl = new ArrayList<>();
        for (int i = 0; i < placeList.size(); i++) {
            for (int j = i + 1; j < placeList.size(); j++) {
                placesUrl.add(getDirectionsUrl(placeList.get(i).getPlaceLatLng(), placeList.get(j).getPlaceLatLng()));
            }
        }

        for (String placeUrlString : placesUrl) {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(placeUrlString);
        }

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
