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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SearchOptionSelectedListener, OnMapReadyCallback, PlaceListItemClickListener,LocationListener, GoogleMap.OnMapClickListener, FindPathUtils.TaskListener {

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
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_loading)
    TextView textLoading;

    private BottomSheetPlaceAdapter adapter;

    private BottomSheetBehavior bottomSheetBehavior;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private FragmentUtils mFragmentUtils;
    private FindPathUtils mFindPathUtils;

    private SearchFragment searchFragment;

    private List<PlaceParent> mSearchData;
    private List<PlaceParent> mSuggestData;
    private List<Place> mPlaceList;
    private List<Place> bottomSheetPlaceList;

    private Location currentLocation;
    private Place currentPlace;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private static final int STATE_SHOWTOOL = 0;
    private static final int STATE_HIDETOOL = 1;

    private int screenState = STATE_SHOWTOOL;

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

        currentPlace = new Place("You",new LatLng(0,0));

        mSearchData = new ArrayList<>();
        mSuggestData = new ArrayList<>();
        mPlaceList = new ArrayList<>();
        bottomSheetPlaceList = new ArrayList<>();

        setupMap();
        setupLocationManager();
        setupBottomSheet();
        setupBottomNav();
        setupSearchBox();
        setupFragment();

        mPlaceList.add(currentPlace);
        mFindPathUtils = new FindPathUtils(mPlaceList,mMap);
        mFindPathUtils.setOnTaskFinishListener(this);

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
            showCurrentLocation();
        }

    }

    /*--------------bottom sheet control section------------------*/

    public void hideBottomNav(){
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.fade_animator);
        animatorSet.setTarget(bottomNavigationView);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                searchContainer.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    public void showBottomNav(){
        bottomNavigationView.setAlpha(1);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }


    /*--------------bottom sheet control section------------------*/
    public void collapseBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        screenState = STATE_SHOWTOOL;
    }

    public void expandBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        screenState = STATE_SHOWTOOL;
    }

    public void hideBottomSheet(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setButtonNoPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        btnAdd.setVisibility(View.VISIBLE);
        textNumPlace.setText(getResources().getString(R.string.no_place));

        //gone
        textGoingTo.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        textTotalTime.setVisibility(View.GONE);
        textLoading.setVisibility(View.GONE);
    }

    private void setButtonHasPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        textGoingTo.setVisibility(View.VISIBLE);
        textTotalDistance.setVisibility(View.VISIBLE);
        textTotalTime.setVisibility(View.VISIBLE);

        //gone
        btnAdd.setVisibility(View.GONE);
        textLoading.setVisibility(View.GONE);
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
        if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED||bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
        else{
            super.onBackPressed();
            hideSearchButtons();
            clearSearchBox();
            showBottomNav();
        }
    }

    @OnClick({R.id.autocomplete_search,R.id.btn_add})
    void pushFragment(){
        if(mFragmentUtils.getBackStackCount()<1){
            hideBottomNav();
            collapseBottomSheet();
            showSearchButtons();
            mFragmentUtils.add(searchFragment);
        }
    }

    /*--------------map control section------------------*/
    @Override
    public void onShowInMapClick(LatLng latLng, String placeTitle) {
        Log.d(TAG, "showInMap: ");
            showLocation(latLng,placeTitle);
            onBackPressed();
    }

    @OnClick(R.id.btn_show_all)
    public void showAllLocation(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(mPlaceList!=null&&mPlaceList.size()>0){
            for(Place place : mPlaceList){
                builder.include(place.getPlaceLatLng());
            }
            LatLngBounds bounds;
            bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,180));
        }
    }

    @OnClick(R.id.btn_show_currrent)
    public void showCurrentLocation(){
        if(currentLocation!=null){
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,15));
        }
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
        for(Place place : excludeCurrentPlace()){
            pinLocation(place.getPlaceLatLng(),place.getPlaceTitle());
        }
    }

    void drawPath(){
        mFindPathUtils.findPath(mMap,mPlaceList);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        switch (screenState) {
            case STATE_SHOWTOOL:
                hideMapTools();
                break;
            case STATE_HIDETOOL:
                collapseBottomSheet();
                break;
        }
    }

    public void hideMapTools(){
        hideBottomSheet();
        hideSearchBar();
        screenState = STATE_HIDETOOL;
    }

    /*--------------place list manage section---------------*/
    @Override
    public void onAddToListClick(Place place) {
        if(!checkHasPlace(bottomSheetPlaceList,place)){
            bottomSheetPlaceList.add(place);

            mFindPathUtils.addPlace(place);
        }
    }

    public boolean checkHasPlace(List<Place> placeList,Place newPlace){
        for(Place place:placeList){
            if(place.getPlaceTitle().contentEquals(newPlace.getPlaceTitle()))
                return true;
        }
        return false;
    }

    @Override
    public void onRemovePlace(int position) {
        mPlaceList = mFindPathUtils.collapseGraph(position+1);
        bottomSheetPlaceList = excludeCurrentPlace();

        adapter.setData(bottomSheetPlaceList);

        updateDistance();
        updateBottomSheet();
        mMap.clear();
        pinAllLocation();

        drawPath();
    }

    public void updateBottomSheet(){
        if(excludeCurrentPlace().size()>0){
            setButtonHasPlace();

            String strNumPlace = getString(R.string.places,excludeCurrentPlace().size());
            textNumPlace.setText(strNumPlace);

            String strGoingTo = getString(R.string.going_to,excludeCurrentPlace().get(0).getPlaceTitle());
            textGoingTo.setText(strGoingTo);

            String strTotalDistance = getString(R.string.total_distance,toDistanceText(mFindPathUtils.getNearestSumDistance()));
            textTotalDistance.setText(strTotalDistance);
        }else{
            setButtonNoPlace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());

        currentPlace.setPlaceLatLng(currentLatLng);
        currentLocation = location;

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


    @Override
    public void OnStartTask() {
        progressBar.setVisibility(View.VISIBLE);

        textGoingTo.setVisibility(View.GONE);
        textTotalTime.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);

        textLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUpdateValue(int value) {
        progressBar.setProgress(value);
    }

    @Override
    public void onFinishTask() {
        textTotalTime.setVisibility(View.VISIBLE);
        textTotalDistance.setVisibility(View.VISIBLE);
        textGoingTo.setVisibility(View.VISIBLE);
        textLoading.setVisibility(View.GONE);

        mPlaceList = mFindPathUtils.getPlaceList();

        adapter.setData(excludeCurrentPlace());
        updateBottomSheet();
        mMap.clear();
        pinAllLocation();

        updateDistance();

        progressBar.setVisibility(View.INVISIBLE);

        drawPath();
    }

    private void updateDistance(){
        int[] nearestPathValue = mFindPathUtils.getNearestPathValue();
        int sum = 0;

        for(int i=0;i<adapter.getData().size();i++){
            sum+=nearestPathValue[i];
            adapter.updateData(i,sum);
        }

    }


    @Override
    public void onDrawPath() {

    }

    private List<Place> excludeCurrentPlace(){
        if(mPlaceList.size()>1)
            return mPlaceList.subList(1,mPlaceList.size());
        return new ArrayList<>();
    }

    public String toDistanceText(int distance){
        if(distance<1000)
            return distance + " " + getString(R.string.m);
        else
            return Math.round(distance/1000f) + " " + getString(R.string.km);
    }

    public String toDurationText(int duration){
        int hours = Math.round(duration/60f);
        int minutes = duration%60;
        String durationText = "";
        if(hours>0)
            durationText = hours + " " + getString(R.string.hr) + " ";
        durationText = durationText + minutes + " " + getString(R.string.min);
        return durationText;
    }
}
