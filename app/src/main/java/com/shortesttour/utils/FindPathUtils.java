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
import java.util.Queue;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;
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
    private boolean cancel = false;

    private int progress = 0;
    private static final int MODE_REQUEST = 0;
    private static final int MODE_UNREQUEST = 1;
    private int progressMode = MODE_REQUEST;
    private static final int MAX_PROGRESS_GET_VALUE = 50;

    public interface TaskListener {
        void OnStartTask();

        void onUpdateValue(int value);

        void onComplete();

        void onCancel();

        void onDrawPath(List<PolylineOptions> lineOptions);
    }

    public FindPathUtils(AppCompatActivity activity) {
        mPlaceList = new ArrayList<>();
        graphUtils = new GraphUtils(activity,this) {
            @Override
            public void setProgress(int val) {
                int totalProgress = progressMode == MODE_REQUEST ? val + progress : val*2;
                publishProgress(totalProgress);
            }
        };


        placeQueue = new LinkedList<>();

        repository = new DirectionApiResultRepository(activity.getApplication());
        //repository.deleteAll();
    }

    public void setOnTaskFinishListener(TaskListener listener) {
        mListener = listener;
    }

    public void collapseGraph(final int position) {
        if(mListener!=null){
            progress = 0;
            mListener.OnStartTask();
        }

        isTaskRunning = true;
        cancel = false;

        progressMode = MODE_UNREQUEST;
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                graphUtils.collapseGraph(position);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", "onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        if(mListener!=null) {
                            if (cancel) {
                                mListener.onCancel();
                            } else {
                                mPlaceList.remove(position);
                                mListener.onComplete();
                            }
                        }
                        findPath();
                    }
                });
    }

    public void findPath() {

        if(placeQueue.size()<1){
                extractPolyline(graphUtils.getRoutes())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new io.reactivex.Observer<List<PolylineOptions>>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(List<PolylineOptions> polylineOptions) {
                                if (mListener != null)
                                    mListener.onDrawPath(polylineOptions);
                              taskFinish();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
    }

    public void addPlace(Place newPlace) {
        placeQueue.add(newPlace);
        if (!isTaskRunning) {
            connectNodes();
        }
    }

    synchronized private void connectNodes() {
        progressMode = MODE_REQUEST;

        startRuntime = System.currentTimeMillis();

        isTaskRunning = true;
        cancel = false;

        final Place newPlace = placeQueue.remove();

        if (mListener != null)
            mListener.OnStartTask();

        Observable.fromCallable(new Callable<int[]>() {
            @Override
            public int[] call(){

                String[] apiResponse = getApiResponse(newPlace);

                GraphNode[] nodes = parseJSONData(apiResponse).blockingSingle();

                graphUtils.expandGraph(nodes);

                return graphUtils.getPath();
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
//                        isTaskRunning = false;
                        try {
                            if (mListener != null){
                                if(cancel){
                                    placeQueue.clear();
                                    mListener.onCancel();
                                }else{
                                    mPlaceList.add(newPlace);
                                    mListener.onComplete();
                                }
                            }

//                            taskFinish();
                            findPath();
                        } catch (Exception e) {
                            Log.e("error", "onPostExecute: ", e);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", "onError: ", e);
                        if (mListener != null) {
                            if (cancel) {
                                placeQueue.clear();
                                mListener.onCancel();
//                                isTaskRunning = false;
                            }
                        }
                        findPath();
                    }

                    @Override
                    public void onComplete() {
                        if (placeQueue.size() > 0)
                            connectNodes();

                        displayRuntime();
                    }
                });
    }

    public void cancelTask(){
        cancel = true;
    }

    public void taskFinish(){
        cancel = false;
        isTaskRunning = false;
    }

    //test
    private void displayRuntime(){
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

    public boolean checkCancel(){
        return cancel;
    }

    private String[] getApiResponse(Place newPlace){

        String[] apiResponse = new String[mPlaceList.size()];

        for (int i = 0; i < apiResponse.length; i++) {
            if(cancel) return null;

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
            Log.e("Exception ", e.toString());
        }
        return data;
    }

    private Observable<DirectionApiResult> fetchFromDb(final double srcLat, final double srcLng,final double desLat,final double desLng){
        return Observable.create(new ObservableOnSubscribe<DirectionApiResult>() {
            @Override
            public void subscribe(ObservableEmitter<DirectionApiResult> emitter) throws Exception {
                DirectionApiResult apiResult = repository.getApiResult(srcLat,srcLng,desLat,desLng);
                if(apiResult == null){
                    apiResult = new DirectionApiResult();
                    apiResult.setSrcLat(srcLat);
                    apiResult.setSrcLng(srcLng);
                    apiResult.setDesLat(desLat);
                    apiResult.setDesLng(desLng);
                    apiResult.setApiResult("");
                }
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {

                    }
                });
                emitter.onNext(apiResult);
                emitter.onComplete();
            }});

    }

    private Observable<String[]> requestAPI(final String... requestUrls) {
        return Observable.fromCallable(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] result = new String[requestUrls.length];

                progress = 0;
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
//                                cancel = true;
//                                return null;
                            }
                        } catch (Exception e) {
                            Log.e("error", "call: ", e);
                        }
                        Log.d("check", "data =  " + result[i]);
                    } while (checkError);
                    progress = (i + 1) * MAX_PROGRESS_GET_VALUE / requestUrls.length;
                    publishProgress(progress);
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
                        if(cancel) return null;

                        JSONParserUtils jsonParserUtils = new JSONParserUtils();
                        jsonObject = new JSONObject(response[i]);

                        parserData[i] = jsonParserUtils.parse(jsonObject);

                    } catch (JSONException e) {
                        Log.e("error", "doInBackground: ", e);
                    }
                    publishProgress(progress);
                }
                return parserData;
            }
        });
    }

    private Observable<List<PolylineOptions>> extractPolyline(final List<List<List<HashMap<String,String>>>> pathList){
        return Observable.fromCallable(new Callable<List<PolylineOptions>>() {
            @Override
            public List<PolylineOptions> call() throws Exception {
                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                List<PolylineOptions> lineList = new ArrayList<>();

                for(int i=0; i < pathList.size(); i++){
                    // Traversing through all the routes
                    List<List<HashMap<String, String>>> paths = pathList.get(i);
                    for (int j = 0; j < paths.size(); j++) {
                        // Fetching i-th route
                        List<HashMap<String, String>> path = paths.get(j);

                        points = new ArrayList<>();
                        lineOptions = new PolylineOptions();

                        // Fetching all the points in i-th route
                        for (int k = 0; k < path.size()-1; k++) {
                            HashMap<String, String> point = path.get(k);

                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);

                            points.add(position);
                        }
                        // Adding all the points in the route to LineOptions
                        lineOptions.width(3);
                        lineOptions.color(Color.RED);
                        lineOptions.addAll(points);
                        lineList.add(lineOptions);
                    }
                }

                return lineList;
            }
        });
    }

    private void publishProgress(int value) {
        if (mListener != null)
            mListener.onUpdateValue(value);
    }

    public void calculatePath(){
        if(mListener!=null){
            progress = 0;
            mListener.OnStartTask();
        }

        isTaskRunning = true;
        cancel = false;

        progressMode = MODE_UNREQUEST;
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                graphUtils.calculatePath();
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e("error", "onError: ",e );
                    }
                    @Override
                    public void onComplete() {
//                        isTaskRunning = false;
                        if(mListener!=null){
                            if (cancel) {
                                mListener.onCancel();
                            }else{
                                mListener.onComplete();
                            }
                            findPath();
                        }
                    }
                });
    }

    public List<Place> getPlaceList() {
        return mPlaceList;
    }

    public List<Place> getOrderedPlaceList(){
        List<Place> orderedPlaceList = new ArrayList<>();
        int[] path = graphUtils.getPath();
        for(int i=0;i<mPlaceList.size();i++){
            orderedPlaceList.add(mPlaceList.get(path[i]));
        }
        return orderedPlaceList;
    }

    public int[] getPath(){
        return graphUtils.getPath();
    }

    public boolean isTaskRunning(){
        return isTaskRunning;
    }

    public GraphNode[][] getGraph(){
        return graphUtils.getGraph();
    }
}