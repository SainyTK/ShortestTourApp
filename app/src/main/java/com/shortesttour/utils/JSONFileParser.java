package com.shortesttour.utils;

import android.app.Activity;
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

    private String fileName;
    private Activity activity;

    public JSONFileParser(Activity activity){
        this.activity = activity;
    }

    public JSONFileParser(Activity activity, String fileName){
        this.fileName = fileName;
        this.activity = activity;
    }

    public JSONArray getJSONArray(){
        JSONArray jsonArray = null;
        try{
            InputStream is = activity.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer,"UTF-8");
            jsonArray = new JSONArray(json);

        }catch(IOException e){
            Log.e("JSONParserFile", "getJSONArray: ", e);
        }catch (JSONException e){
            Log.e("JSONParserFile", "getJSONArray: ", e);
        }

        return jsonArray;
    }


}
