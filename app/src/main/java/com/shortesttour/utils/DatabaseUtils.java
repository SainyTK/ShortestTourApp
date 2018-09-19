package com.shortesttour.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shortesttour.models.PlaceData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {

    Activity activity;

    public DatabaseUtils(Activity activity){
        this.activity = activity;
    }

    public void initializeDatabase(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        DatabaseReference places = myRef.child("places");
        myRef.setValue(createInitialData(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(activity, "Upload Data Complete", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public List<PlaceData> createInitialData(){
        List<PlaceData> data = new ArrayList<>();
        JSONFileParser jsonFileParser = new JSONFileParser(activity,"node.json");

        JSONArray jsonArray = jsonFileParser.getJSONArray();

        int len = jsonArray.length();
        for(int i=0;i<len;i++){
            try{
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                PlaceData placeData = new PlaceData();
                placeData.setPlaceId(i+"");
                placeData.setUserName(jsonObj.getString("userName"));
                placeData.setPlaceTitle(jsonObj.getString("placeTitle"));
                placeData.setLatitude(jsonObj.getDouble("latitude"));
                placeData.setLongitude(jsonObj.getDouble("longitude"));

                List<String> pathList = new ArrayList<>();
                for(int j=0;j<len;j++)
                    pathList.add("");

                placeData.setJsonPaths(pathList);

                data.add(placeData);
            }catch(JSONException e){
                Log.e("error", "JSON Error: ", e);
            }

        }
        return data;
    }
}
