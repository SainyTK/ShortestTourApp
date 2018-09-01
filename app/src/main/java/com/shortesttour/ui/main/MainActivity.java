package com.shortesttour.ui.main;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shortesttour.R;
import com.shortesttour.ui.search.PlaceParent;
import com.shortesttour.ui.search.SearchFragment;
import com.shortesttour.ui.search.SearchOptionSelectedListener;
import com.shortesttour.utils.FragmentUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SearchOptionSelectedListener, OnMapReadyCallback {

    private static final String TAG = "MainActivity";

    @BindView(R.id.autocomplete_search)
    EditText autoCompleteTextView;
    @BindView(R.id.search_back_btn)
    ImageView searchBackButton;
    @BindView(R.id.search_clear_btn)
    ImageView searchClearButton;
    @BindView(R.id.bottom_sheet_container)
    LinearLayout bottomSheetContainer;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.search_container)
    View searchContainer;

    private BottomSheetBehavior bottomSheetBehavior;
    private GoogleMap mMap;

    private FragmentUtils mFragmentUtils;

    private SearchFragment searchFragment;

    private List<PlaceParent> mSearchData;
    private List<PlaceParent> mSuggestData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mSearchData = new ArrayList<>();
        mSuggestData = new ArrayList<>();

        setupMap();
        setupBottomSheet();
        setupBottomNav();
        setupSearchBox();
        setupFragment();
    }

    /*--------------setup section------------------*/
    private void setupMap(){
        SupportMapFragment mapFragment = new SupportMapFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.map, mapFragment);
        ft.commit();

        mapFragment.getMapAsync(this);
    }

    private void setupBottomSheet(){
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetContainer.setActivated(false);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        bottomSheetContainer.setActivated(false);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheetContainer.setActivated(true);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bottomSheetContainer.setActivated(false);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        bottomSheetContainer.setActivated(false);
                        break;

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d(TAG, "onSlide: offset = " + slideOffset);
                searchContainer.setAlpha(1-slideOffset);
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

    private void setupSearchBox(){
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchData = searchFragment.getPlaceList();
                mSuggestData.clear();
                for(PlaceParent place : mSearchData){
                    if(place.getPlaceTitle().contains(s)){
                        mSuggestData.add(place);
                    }
                }
                searchFragment.setSearchDataSet(mSuggestData);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        hideSearchButtons();
    }

    private void setupFragment(){
        mFragmentUtils = new FragmentUtils(this,R.id.fragment_container);

        searchFragment = new SearchFragment();
        searchFragment.setOptionSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_json));
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

    /*--------------bottom sheet control section------------------*/
    public void collapseBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void expandBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void hideBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /*--------------search control section------------------*/
    private void showSearchButtons(){
        searchBackButton.setVisibility(View.VISIBLE);
        searchClearButton.setVisibility(View.VISIBLE);
    }

    private void hideSearchButtons(){
        searchBackButton.setVisibility(View.GONE);
        searchClearButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.search_clear_btn)
    void clearSearchBox(){
        autoCompleteTextView.setText("");
    }

    /*--------------fragment control section------------------*/
    @OnClick(R.id.search_back_btn)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideSearchButtons();
        clearSearchBox();
    }

    @OnClick(R.id.autocomplete_search)
    void pushFragment(){
        if(mFragmentUtils.getBackStackCount()<1){
            showSearchButtons();
            mFragmentUtils.add(searchFragment);
        }
    }

    /*--------------map control section------------------*/
    @Override
    public void showInMap(LatLng latLng, String placeTitle) {
        Log.d(TAG, "showInMap: ");
            showLocation(latLng,placeTitle);
            onBackPressed();
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
