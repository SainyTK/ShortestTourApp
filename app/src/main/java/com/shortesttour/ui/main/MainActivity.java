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
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.ui.search.SearchFragment;
import com.shortesttour.ui.search.SearchOptionSelectedListener;
import com.shortesttour.utils.FindPathUtils;
import com.shortesttour.utils.FragmentUtils;
import com.shortesttour.utils.PinUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;

public class MainActivity extends AppCompatActivity implements SearchOptionSelectedListener, OnMapReadyCallback, PlaceListItemClickListener,LocationListener, GoogleMap.OnMapClickListener, FindPathUtils.TaskListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.autocomplete_search)
    EditText autoCompleteTextView;
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
    @BindView(R.id.btn_show_line)
    FloatingActionButton btnShowLine;
    @BindView(R.id.btn_show_location)
    FloatingActionButton btnShowLocation;
    @BindView(R.id.btn_showlocation_container)
    RelativeLayout btnShowLocationContainer;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.text_loading)
    TextView textLoading;
    @BindView(R.id.search_back_btn)
    ImageView searchBackButton;
    @BindView(R.id.search_clear_btn)
    ImageView searchClearButton;

    private BottomSheetPlaceAdapter adapter;

    private BottomSheetBehavior bottomSheetBehavior;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private FragmentUtils mFragmentUtils;
    private FindPathUtils mFindPathUtils;
    private PinUtils mPinUtils;

    private SearchFragment searchFragment;

    private List<Place> mSearchData;
    private List<Place> mSuggestData;

    private List<Place> mPlaceList;
    private List<Place> bottomSheetPlaceList;
    private List<PolylineOptions> mLineList;

    private Location currentLocation;
    private Place currentPlace;
    private boolean mLocationPermissionGranted;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private static final int STATE_SHOWTOOL = 0;
    private static final int STATE_HIDETOOL = 1;

    private static final int STATE_SHOW_NO_LINE = 0;
    private static final int STATE_SHOW_LINE = 1;
    private static final int STATE_SHOW_ALL_LINE = 2;

    private static final int STATE_SHOW_CURRENT_LOCATION = 0;
    private static final int STATE_SHOW_ALL_LOCATION = 1;

    private int screenState = STATE_SHOWTOOL;
    private int showLineState = STATE_SHOW_NO_LINE;
    private int showPlaceState = STATE_SHOW_CURRENT_LOCATION;

    private Observable<List<String>> observable;

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

        Log.d(TAG, "onCreate: START");

        mPinUtils = new PinUtils(this);
//
        currentPlace = new Place(0,"You","Your Location",0,0);

        mSuggestData = new ArrayList<>();
        mSearchData = new ArrayList<>();
        mPlaceList = new ArrayList<>();
        bottomSheetPlaceList = new ArrayList<>();
        mLineList = new ArrayList<>();
//
        setupMap();
        setupLocationManager();
        setupBottomSheet();
        setupBottomNav();
        setupFragment();
        setupSearchBox();
        updateShowLineButton();

        mPlaceList.add(currentPlace);
        mFindPathUtils = new FindPathUtils(this,mPlaceList);
        mFindPathUtils.setOnTaskFinishListener(this);
    }

    /*--------------A: setup section------------------*/
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
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20, 5, this);
    }

    private void getLocationPermission() {
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
                btnShowLocation.setAlpha(alpha);
                btnShowLine.setAlpha(alpha);

                if(slideOffset<=-0.75f){
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) btnShowLocationContainer.getLayoutParams();
                    layoutParams.setAnchorId(R.id.space);
                    btnShowLocationContainer.setLayoutParams(layoutParams);
                }else{
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) btnShowLocationContainer.getLayoutParams();
                    layoutParams.setAnchorId(R.id.bottom_sheet_container);
                    btnShowLocationContainer.setLayoutParams(layoutParams);
                }

                if(alpha==0){
                    searchContainer.setVisibility(View.GONE);
                    btnShowLocation.setVisibility(View.GONE);
                    btnShowLine.setVisibility(View.GONE);
                }
                else{
                    searchContainer.setVisibility(View.VISIBLE);
                    btnShowLocation.setVisibility(View.VISIBLE);
                    btnShowLine.setVisibility(View.VISIBLE);
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

    private void setupFragment(){
        mFragmentUtils = new FragmentUtils(this,R.id.fragment_container);

        searchFragment = new SearchFragment();
        searchFragment.setOptionSelectedListener(this);
    }

    private void setupSearchBox(){
        mSearchData = new ArrayList<>();
        mSuggestData = new ArrayList<>();

        autoCompleteTextView.setInputType(InputType.TYPE_NULL);

        autoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    searchBoxClick();
                }
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchData = searchFragment.getPlaceList();
                mSuggestData.clear();
                for(Place place : mSearchData){
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

    /*--------------B: bottom sheet control section------------------*/

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

    public void updateBottomSheet(){
        if(excludeCurrentPlace().size()>0){
            setButtonHasPlace();

            String strNumPlace = getString(R.string.places,excludeCurrentPlace().size());
            textNumPlace.setText(strNumPlace);

            String strGoingTo = getString(R.string.going_to,excludeCurrentPlace().get(0).getPlaceTitle());
            textGoingTo.setText(strGoingTo);

            String strTotalDistance = getString(R.string.total_distance,toDistanceText(mFindPathUtils.getNearestSumDistance()));
            textTotalDistance.setText(strTotalDistance);

            String strTotalDuration = getString(R.string.total_duration,toDurationText(mFindPathUtils.getNearestSumDuration()));
            textTotalTime.setText(strTotalDuration);
        }else{
            setButtonNoPlace();
        }
    }

    public String toDistanceText(int distance){
        if(distance<1000)
            return distance + " " + getString(R.string.m);
        else
            return Math.round(distance/1000f) + " " + getString(R.string.km);
    }

    public String toDurationText(int duration){
        int hours = Math.round(duration/3600);
        int minutes = Math.round(duration/3600%60);
        String durationText = "";
        if(hours>0)
            durationText = hours + " " + getString(R.string.hr) + " ";
        durationText = durationText + minutes + " " + getString(R.string.min);
        return durationText;
    }

    private void updateDistance(){
        int[] nearestPathValue = mFindPathUtils.getNearestDistance();
        int sum = 0;

        for(int i=0;i<adapter.getData().size();i++){
            sum+=nearestPathValue[i];
            adapter.updateData(i,sum);
        }
    }

    /*--------------C: search control section------------------*/

    private void showSearchButtons(){
        searchBackButton.setVisibility(View.VISIBLE);
        searchClearButton.setVisibility(View.VISIBLE);
    }

    private void hideSearchButtons(){
        searchBackButton.setVisibility(View.GONE);
        searchClearButton.setVisibility(View.GONE);
    }

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

    @OnClick(R.id.search_clear_btn)
    void clearSearchBox(){
        autoCompleteTextView.setText("");
    }

    /*--------------D: fragment control section------------------*/
    @OnClick(R.id.search_back_btn)
    @Override
    public void onBackPressed() {
        if(mFragmentUtils.getBackStackCount()>0){
            super.onBackPressed();
            clearSearchBox();
            hideSearchButtons();
            hideKeyboard();
            autoCompleteTextView.setInputType(InputType.TYPE_NULL);
        }
        else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED||bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
        else{
            super.onBackPressed();
        }
    }

    @OnClick({R.id.btn_add,R.id.autocomplete_search})
    void searchBoxClick(){
        pushFragment();
        showSearchButtons();
        autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    void pushFragment(){
        if(mFragmentUtils.getBackStackCount()<1){
            collapseBottomSheet();
            mFragmentUtils.replace(searchFragment,true);
        }
    }

    /*--------------E: map control section------------------*/
    @Override
    public void onShowInMapClick(LatLng latLng, String placeTitle) {
        Log.d(TAG, "showInMap: ");
            showLocation(latLng,placeTitle);
            onBackPressed();
    }

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
        showPlaceState = STATE_SHOW_ALL_LOCATION;
        updateShowLocationButton();
    }

    public void showCurrentLocation(){
        if(currentLocation!=null){
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,17));
            showPlaceState = STATE_SHOW_CURRENT_LOCATION;
            updateShowLocationButton();
        }
    }

    public void showLocation(LatLng latLng,String placeTitle){
        float zoom = mMap.getCameraPosition().zoom;
        if(zoom < 10)
            zoom = 16;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        for(Place place : excludeCurrentPlace()){
            if(placeTitle.contentEquals(place.getPlaceTitle()))
                return;
        }
        mMap.addMarker(new MarkerOptions().title(placeTitle).position(latLng));
    }

    public void pinLocation(LatLng latLng,String placeTitle,int num){
        mMap.addMarker(new MarkerOptions().position(latLng).title(placeTitle).icon(BitmapDescriptorFactory.fromBitmap(mPinUtils.createNumberPin(num))));
    }

    public void pinAllLocation(){
        List<Place> placeList = excludeCurrentPlace();
        for(int i=0;i<placeList.size();i++){
            Place place = placeList.get(i);
            pinLocation(place.getPlaceLatLng(),place.getPlaceTitle(),i);
        }
    }

    void findPath(){
        mLineList.clear();
        mFindPathUtils.findPath();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        switch (screenState) {
            case STATE_SHOWTOOL:
                if(mFragmentUtils.getBackStackCount()==0)
                    hideMapTools();
                break;
            case STATE_HIDETOOL:
                collapseBottomSheet();
                break;
        }
    }

    public void clearLine(){
        mMap.clear();
        pinAllLocation();
    }

    private void drawPath(int i){
        if(i<mLineList.size()){
            mMap.addPolyline(mLineList.get(i));
        }
    }

    private void drawAllPath(){
        for(PolylineOptions line:mLineList){
            mMap.addPolyline(line);
        }
    }

    public void showCurrentLine(){
        clearLine();
        drawPath(0);
        showLineState = STATE_SHOW_LINE;
        updateShowLineButton();
    }

    public void showAllLine(){
        clearLine();
        drawAllPath();
        showLineState = STATE_SHOW_ALL_LINE;
        updateShowLineButton();
    }

    /*------------------F: Map Tools Section------------------*/
    public void hideMapTools(){
        hideBottomSheet();
        hideSearchBar();
        screenState = STATE_HIDETOOL;
    }

    @OnClick(R.id.btn_show_location)
    void handleShowLocation(){
        switch (showPlaceState){
            case STATE_SHOW_CURRENT_LOCATION :
                showAllLocation();
                break;
            case STATE_SHOW_ALL_LOCATION :
                showCurrentLocation();
                break;
        }
    }

    private void updateShowLocationButton(){
        switch (showPlaceState){
            case STATE_SHOW_CURRENT_LOCATION :
                btnShowLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location));
                break;
            case STATE_SHOW_ALL_LOCATION :
                btnShowLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin));
                break;
        }
    }

    @OnClick(R.id.btn_show_line)
    void handleShowLine(){
        switch (showLineState){
            case STATE_SHOW_LINE :
                showAllLine();
                break;
            case STATE_SHOW_ALL_LINE :
                showCurrentLine();
                break;
        }
    }

    private void updateShowLineButton(){
        if(mLineList.size()==0)
            showLineState = STATE_SHOW_NO_LINE;
        switch (showLineState){
            case STATE_SHOW_NO_LINE :
                btnShowLine.setImageDrawable(getResources().getDrawable(R.drawable.ic_show_line));
                btnShowLine.setEnabled(false);
                break;
            case STATE_SHOW_LINE :
                btnShowLine.setImageDrawable(getResources().getDrawable(R.drawable.ic_show_line));
                btnShowLine.setEnabled(true);
                break;
            case STATE_SHOW_ALL_LINE :
                btnShowLine.setImageDrawable(getResources().getDrawable(R.drawable.ic_show_lines));
                btnShowLine.setEnabled(true);
                break;
        }
    }


    /*--------------G: place list manage section---------------*/
    @Override
    public void onAddToListClick(Place place) {

//        List<Place> searchPlaceList = searchFragment.getPlaceList();
//        for(int i=0;i<60;i++){
//            Place p = searchPlaceList.get(i);
//            bottomSheetPlaceList.add(p);
//            mFindPathUtils.addPlace(p);
//        }
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
        updateShowLineButton();
        mMap.clear();
        pinAllLocation();

        findPath();
    }

    private List<Place> excludeCurrentPlace(){
        if(mPlaceList.size()>1)
            return mPlaceList.subList(1,mPlaceList.size());
        return new ArrayList<>();
    }

    /*---------------------Result Handle Section---------------*/

    @Override
    public void OnStartTask(String placeTitle) {
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        textGoingTo.setVisibility(View.GONE);
        textTotalTime.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        btnAdd.setVisibility(View.GONE);

        textLoading.setVisibility(View.VISIBLE);

        String findingPathStr = getString(R.string.finding_path,placeTitle);
        Toast.makeText(this, findingPathStr, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateValue(int value) {
//        Log.d(TAG, "onUpdateValue: " + value);
        progressBar.setProgress(value);
    }

    @Override
    public void onFinishTask(int[] path) {
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

        //String findingPathStr = getString(R.string.added_to_list,placeTitle);
        //Toast.makeText(this, findingPathStr, Toast.LENGTH_SHORT).show();
        findPath();
    }

    @Override
    public void onDrawPath(PolylineOptions polylineOptions) {
        if(polylineOptions!=null){
            if(mLineList.size()==0)
                polylineOptions.color(getResources().getColor(R.color.activeTint));
            mLineList.add(polylineOptions);
            showAllLine();
        }else{
            Toast.makeText(this, "Error Get Path", Toast.LENGTH_SHORT).show();
        }
    }


    /*---------------------Location Manage Section-------------*/
    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());

        currentPlace.setPlaceLatLng(currentLatLng);
        currentLocation = location;
    }

    /*-----------------------SQLite Database Section----------------*/

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    /*---------------------Other Section-----------------------*/
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private List<Place> updatePlaceList(int[] path) {
        List<Place> sortedPlace = new ArrayList<>();
        int pathLength = path.length;

        for (int i = 0; i < pathLength; i++) {
            sortedPlace.add(mPlaceList.get(path[i]));
        }
        return  sortedPlace;
    }
}
