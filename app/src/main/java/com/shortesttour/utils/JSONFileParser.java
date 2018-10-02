package com.shortesttour.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.models.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JSONFileParser {

    public static List<Place> getPlaces(@NonNull Activity activity,@NonNull String fileName){
        List<Place> placeList = null;
        try{
            InputStream is = activity.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer,"UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            placeList = new ArrayList<>();
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Place place = new Place();

                place.setPlaceId(i+1);
                place.setPlaceTitle(jsonObject.getString("userName"));
                place.setUserName(jsonObject.getString("userName"));
                place.setLatitude(Double.parseDouble(jsonObject.getString("latitude")));
                place.setLongitude(Double.parseDouble(jsonObject.getString("longitude")));

                placeList.add(place);
            }
        }catch(IOException e){
            Log.e("JSONParserFile", "getJSONArray: ", e);
        }catch (JSONException e){
            Log.e("JSONParserFile", "getJSONArray: ", e);
        }

        return placeList;
    }


}
