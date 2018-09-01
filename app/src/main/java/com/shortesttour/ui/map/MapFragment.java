package com.shortesttour.ui.map;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shortesttour.R;
import com.shortesttour.ui.main.MainActivity;
import com.shortesttour.utils.FragmentUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";

    private MainActivity mainActivity;

    private GoogleMap mMap;
    private FragmentUtils fragmentUtils;

    @BindView(R.id.bottom_sheet_container)
    LinearLayout bottomSheetContainer;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    private BottomSheetBehavior bottomSheetBehavior;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.from(getContext()).inflate(R.layout.fragment_map,container,false);
        ButterKnife.bind(this,view);

        mainActivity = (MainActivity)getActivity();

        fragmentUtils = new FragmentUtils(mainActivity,R.id.map);

        SupportMapFragment mapFragment = new SupportMapFragment();
        fragmentUtils.replace(mapFragment,false);
        mapFragment.getMapAsync(this);

        setupBottomSheet();
        setupBottomNav();

        return view;
    }

    private void setupBottomSheet(){
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "onStateChanged: collapse");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "onStateChanged: dragging");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d(TAG, "onStateChanged: expanded");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "onStateChanged: hidden");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d(TAG, "onStateChanged: settling");
                        break;

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void setupBottomNav(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                switch (item.getItemId()){
                    case R.id.menu_driving:
                        collapseBottomSheet();
                        return true;
                }
                return false;
            }
        });
    }

    public void collapseBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void expandBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void hideBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.style_json));
            if(!success){
                Log.d(TAG, "onMapReady: style parsing failed");
            }
        }catch(Resources.NotFoundException e){
            Log.d(TAG, "onMapReady: Can't find style ",e);
        }

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        showLocation(sydney,"Sydney");
    }

    public void showLocation(LatLng latLng,String placeTitle){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(placeTitle));

        float zoom = mMap.getCameraPosition().zoom;
        if(zoom < 10)
            zoom = 16;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

}
