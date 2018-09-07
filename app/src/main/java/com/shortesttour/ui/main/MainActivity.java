package com.shortesttour.ui.main;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.ui.search.PlaceParent;
import com.shortesttour.ui.search.SearchFragment;
import com.shortesttour.ui.search.SearchOptionSelectedListener;
import com.shortesttour.utils.FindPathUtils;
import com.shortesttour.utils.FragmentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SearchOptionSelectedListener, OnMapReadyCallback, PlaceListItemClickListener,LocationListener, GoogleMap.OnMapClickListener {

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
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.btn_add)
    TextView btnAdd;
    @BindView(R.id.btn_start)
    TextView btnStart;
    @BindView(R.id.text_num_place)
    TextView textNumPlace;
    @BindView(R.id.text_going_to)
    TextView textGoingTo;
    @BindView(R.id.text_total_distance)
    TextView textTotalDistance;
    @BindView(R.id.text_total_time)
    TextView textTotalTime;
    @BindView(R.id.btn_showall_container)
    RelativeLayout btnShowAllContainer;
    @BindView(R.id.btn_show_all)
    FloatingActionButton btnShowAll;
    @BindView(R.id.btn_show_currrent)
    FloatingActionButton btnShowCurrent;

    private BottomSheetPlaceAdapter adapter;

    private BottomSheetBehavior bottomSheetBehavior;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private FragmentUtils mFragmentUtils;
    private FindPathUtils mFindPathUtils;

    private SearchFragment searchFragment;

    private List<PlaceParent> mSearchData;
    private List<PlaceParent> mSuggestData;

    private Location currentLocation;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

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

        mFindPathUtils = new FindPathUtils();

        setupMap();
        setupLocationManager();
        setupBottomSheet();
        setupBottomNav();
        setupSearchBox();
        setupFragment();
    }

    /*--------------setup section------------------*/
    private void setupMap() {
        SupportMapFragment mapFragment = new SupportMapFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.map, mapFragment);
        ft.commit();

        mapFragment.getMapAsync(this);

        getLocationPermission();
    }

    private void setupLocationManager() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationPermissionGranted)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
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
                Log.d(TAG, "onSlide: " + slideOffset);
                float alpha = 1 - slideOffset*1.5f;
                searchContainer.setAlpha(alpha);
                btnShowAll.setAlpha(alpha);
                btnShowCurrent.setAlpha(alpha);

                if(slideOffset<=-0.75f){
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) btnShowAllContainer.getLayoutParams();
                    layoutParams.setAnchorId(R.id.space);
                    btnShowAllContainer.setLayoutParams(layoutParams);
                }else{
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) btnShowAllContainer.getLayoutParams();
                    layoutParams.setAnchorId(R.id.bottom_sheet_container);
                    btnShowAllContainer.setLayoutParams(layoutParams);
                }

                if(alpha==0){
                    searchContainer.setVisibility(View.GONE);
                    btnShowAll.setVisibility(View.GONE);
                    btnShowCurrent.setVisibility(View.GONE);
                }
                else{
                    searchContainer.setVisibility(View.VISIBLE);
                    btnShowCurrent.setVisibility(View.VISIBLE);
                    btnShowAll.setVisibility(View.VISIBLE);
                }
            }
        });

        setupRecyclerView();
    }

    private void setupRecyclerView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new BottomSheetPlaceAdapter(new ArrayList<Place>());
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
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
        mMap.setOnMapClickListener(this);

        try{
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_json));
            if(!success){
                Log.d(TAG, "onMapReady: style parsing failed");
            }
        }catch(Resources.NotFoundException e){
            Log.d(TAG, "onMapReady: Can't find style ",e);
        }

        if(mLocationPermissionGranted){
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

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

    private void setButtonNoPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        btnAdd.setVisibility(View.VISIBLE);
        textNumPlace.setText("No Places");

        //gone
        btnStart.setVisibility(View.GONE);
        textGoingTo.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        textTotalTime.setVisibility(View.GONE);
    }

    private void setButtonHasPlace(boolean isStart){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.VISIBLE);
        textGoingTo.setVisibility(View.VISIBLE);

        //gone
        btnAdd.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        textTotalTime.setVisibility(View.GONE);

        if(isStart){
            textTotalDistance.setVisibility(View.VISIBLE);
            textTotalTime.setVisibility(View.VISIBLE);
        }
    }

    /*--------------search control section------------------*/

    public void hideSearchBar(){
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.fade_animator);
        animatorSet.setTarget(searchContainer);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                searchContainer.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

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

    @OnClick({R.id.autocomplete_search,R.id.btn_add})
    void pushFragment(){
        if(mFragmentUtils.getBackStackCount()<1){
            collapseBottomSheet();
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

    @OnClick(R.id.btn_show_all)
    public void showAllLocation(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<Place> mPlaceList = adapter.getData();
        if(mPlaceList!=null&&mPlaceList.size()>0){
            for(Place place:mPlaceList){
                builder.include(place.getPlaceLatLng());
            }
            LatLngBounds bounds;
            bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,60));
        }
    }

    @OnClick(R.id.btn_show_currrent)
    public void showCurrentLocation(){
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,15));
    }

    public void showLocation(LatLng latLng,String placeTitle){

        mMap.clear();
        pinLocation(latLng,placeTitle);

        float zoom = mMap.getCameraPosition().zoom;
        if(zoom < 10)
            zoom = 16;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    public void pinLocation(LatLng latLng,String placeTitle){
        mMap.addMarker(new MarkerOptions().position(latLng).title(placeTitle));
    }

    public void pinAllLocation(){
        List<Place> mPlaceList = adapter.getData();
        for(Place place:mPlaceList){
            pinLocation(place.getPlaceLatLng(),place.getPlaceTitle());
        }
    }

    @OnClick(R.id.btn_start)
    void showPath(){
        mFindPathUtils.findPath(mMap,adapter.getData());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideMapTools();
    }

    public void hideMapTools(){
        hideBottomSheet();
        hideSearchBar();
    }

    /*--------------place list manage section---------------*/
    @Override
    public void addToList(Place place) {
        List<Place> mPlaceList = adapter.getData();


        if(!checkHasPlace(mPlaceList,place)){
            adapter.addPlace(place);
            updateBottomSheet();
            pinLocation(place.getPlaceLatLng(),place.getPlaceTitle());

            mMap.clear();
            pinAllLocation();
        }
    }

    private boolean checkHasPlace(List<Place> placeList,Place newPlace){
        for(Place place:placeList){
            if(place.getPlaceTitle().contentEquals(newPlace.getPlaceTitle()))
                return true;
        }
        return false;
    }

    @Override
    public void onRemovePlace(int position) {
        adapter.removePlace(position);

        updateBottomSheet();
        mMap.clear();
        pinAllLocation();
    }

    public void updateBottomSheet(){
        List<Place> mPlaceList = adapter.getData();
        if(mPlaceList.size()>0){
            setButtonHasPlace(false);

            textNumPlace.setText(mPlaceList.size() + " places");
            textGoingTo.setText("Going to " + mPlaceList.get(0).getPlaceTitle());
        }else{
            setButtonNoPlace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        Log.d(TAG, "onLocationChanged: lat = " + location.getLatitude() + "lng = " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
