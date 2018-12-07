package com.shortesttour.ui.main;

import android.Manifest;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.shortesttour.events.AddPlaceEvent;
import com.shortesttour.events.RemovePlaceEvent;
import com.shortesttour.events.ShowInMapEvent;
import com.shortesttour.events.SwapPlaceEvent;
import com.shortesttour.models.Place;
import com.shortesttour.ui.search.SearchFragment;
import com.shortesttour.ui.select_algoritm.SelectAlgorithmFragment;
import com.shortesttour.ui.travel.TravelFragment;
import com.shortesttour.utils.FragmentUtils;
import com.shortesttour.utils.JSONFileParser;
import com.shortesttour.utils.PinUtils;
import com.shortesttour.utils.PrefsUtil;
import com.shortesttour.utils.TestUtils;

import junit.framework.Test;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainContract.View, OnMapReadyCallback, GoogleMap.OnMapClickListener, SelectAlgorithmFragment.ChangeAlgorithmListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.autocomplete_search)
    EditText autoCompleteTextView;
    @BindView(R.id.bottom_sheet_container)
    LinearLayout bottomSheetContainer;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.search_container)
    View searchContainer;
    @BindView(R.id.btn_show_line)
    FloatingActionButton btnShowLine;
    @BindView(R.id.btn_show_location)
    FloatingActionButton btnShowLocation;
    @BindView(R.id.btn_showlocation_container)
    RelativeLayout btnShowLocationContainer;
    @BindView(R.id.search_back_btn)
    ImageView searchBackButton;
    @BindView(R.id.search_clear_btn)
    ImageView searchClearButton;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private BottomSheetBehavior bottomSheetBehavior;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private FragmentUtils mFragmentUtils;
    private FragmentUtils bottomFragmentUtils;

    private SearchFragment searchFragment;
    private TravelFragment travelFragment;
    private SelectAlgorithmFragment selectAlgorithmFragment;

    private List<PolylineOptions> mLineList;

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

    private MainPresenter mPresenter;

    private boolean isTaskRunning = false;
    private boolean changeAlgorithm = false;
    private int prevAlgorithm;

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

        mPresenter = new MainPresenter(this);

        setupMap();
        setupBottomSheet();

        setupSearchBox();
        setupBottomNav();

        setupLineManager();

        prevAlgorithm = PrefsUtil.getAlgorithm(this);

    }

    private void setupLineManager() {
        mLineList = new ArrayList<>();
        updateShowLineButton();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
            if (!success) {
                Log.d(TAG, "onMapReady: style parsing failed");
            }
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "onMapReady: Can't find style ", e);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        setupLocationManager();

        createPlaceList();
//        startTest();
    }

    List<Place> placeList;
    boolean collecting = true;

    //test
    public void createPlaceList(){
        List<Place> places = null;
        try{
            places = JSONFileParser.getPlaces(getActivity(),getResources().getString(R.string.node_file_name));
        }catch (NullPointerException e){
            Log.e(TAG, "createPlaceList: NULL", e);
        }
        placeList = places;
    }

    //test
    private void startTest(){
        startNN();
    }

    //test
    public void startNN(){
        PrefsUtil.setAlgorithm(this,PrefsUtil.NEAREST_NEIGHBOR);
        TestUtils.getInstance().setStartTextNN();
        for(int r : TestUtils.getInstance().getRandomIndex()){
            mPresenter.addPlace(placeList.get(r));
        }
    }

    //test
    public void startDP(){
        PrefsUtil.setAlgorithm(this,PrefsUtil.DYNAMIC_PROGRAMMING);
        TestUtils.getInstance().setStartTextDP();
        for(int r : TestUtils.getInstance().getRandomIndex()){
            mPresenter.addPlace(placeList.get(r));
        }
    }

    public void showResult() {
        TestUtils.getInstance().setPlaceList(mPresenter.getPlaceList());
        TestUtils.getInstance().showResultTable();

        removeAll();
    }

    private void setupLocationManager() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationPermissionGranted)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: " + location.getLatitude() + "," + location.getLongitude());
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mPresenter.setupCurrentPlace(currentLatLng);
                showLocation(currentLatLng);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            @Override
            public void onProviderEnabled(String s) {}
            @Override
            public void onProviderDisabled(String s) {}
        }, null);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
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
                float alpha = 1 - slideOffset * 1.5f;
                searchContainer.setAlpha(alpha);
                btnShowLocation.setAlpha(alpha);
                btnShowLine.setAlpha(alpha);

                if (slideOffset <= -0.75f) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) btnShowLocationContainer.getLayoutParams();
                    layoutParams.setAnchorId(R.id.space);
                    btnShowLocationContainer.setLayoutParams(layoutParams);
                } else {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) btnShowLocationContainer.getLayoutParams();
                    layoutParams.setAnchorId(R.id.bottom_sheet_container);
                    btnShowLocationContainer.setLayoutParams(layoutParams);
                }

                if (alpha == 0) {
                    searchContainer.setVisibility(View.GONE);
                    btnShowLocation.setVisibility(View.GONE);
                    btnShowLine.setVisibility(View.GONE);
                } else {
                    searchContainer.setVisibility(View.VISIBLE);
                    btnShowLocation.setVisibility(View.VISIBLE);
                    btnShowLine.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void setupBottomNav() {
        setupBottomFragment();
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                collapseBottomSheet();
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                switch (item.getItemId()) {
                    case R.id.menu_driving:
                        bottomFragmentUtils.replace(travelFragment, false);
                        Log.d(TAG, "onNavigationItemSelected: count = " + bottomFragmentUtils.getBackStackCount());
                        return true;
                    case R.id.menu_algorithm:
                        bottomFragmentUtils.replace(selectAlgorithmFragment, false);
                        Log.d(TAG, "onNavigationItemSelected: count = " + bottomFragmentUtils.getBackStackCount());
                        return true;
                }
                return false;
            }
        });
    }

    private void setupSearchFragment() {
        mFragmentUtils = new FragmentUtils(this, R.id.fragment_container);

        searchFragment = new SearchFragment();
    }

    private void setupBottomFragment() {
        travelFragment = new TravelFragment();
        selectAlgorithmFragment = new SelectAlgorithmFragment();

        selectAlgorithmFragment.setListener(this);

        bottomFragmentUtils = new FragmentUtils(this, R.id.bottom_fragment_container);
        bottomFragmentUtils.replace(travelFragment, false);
    }

    private void setupSearchBox() {
        setupSearchFragment();
        autoCompleteTextView.setInputType(InputType.TYPE_NULL);

        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                autoCompleteTextView.performClick();
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    openSearchPage();
                Log.d(TAG, "onTouch: ");
                return false;
            }

        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPresenter.setSearchData(searchFragment.getPlaceList());
                mPresenter.clearSuggestData();
                for (Place place : mPresenter.getSearchData()) {
                    if (place.getPlaceTitle().contains(s)) {
                        mPresenter.addSuggestData(place);
                    }
                }
                searchFragment.setSearchDataSet(mPresenter.getSuggestData());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        hideSearchButtons();
    }

    /*--------------B: bottom sheet control section------------------*/

    public void hideBottomNav() {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fade_animator);
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

    public void showBottomNav() {
        bottomNavigationView.setAlpha(1);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void collapseBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        screenState = STATE_SHOWTOOL;
    }

    public void expandBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        screenState = STATE_SHOWTOOL;
    }

    public void hideBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /*--------------C: search control section------------------*/

    private void showSearchButtons() {
        searchBackButton.setVisibility(View.VISIBLE);
        searchClearButton.setVisibility(View.VISIBLE);
    }

    private void hideSearchButtons() {
        searchBackButton.setVisibility(View.GONE);
        searchClearButton.setVisibility(View.GONE);
    }

    public void hideSearchBar() {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.fade_animator);
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
    void clearSearchBox() {
        autoCompleteTextView.setText("");
    }

    /*--------------D: fragment control section------------------*/
    @OnClick(R.id.search_back_btn)
    @Override
    public void onBackPressed() {
        if (mFragmentUtils.getBackStackCount() > 0) {
            super.onBackPressed();
            clearSearchBox();
            hideSearchButtons();
            hideKeyboard();
            autoCompleteTextView.setInputType(InputType.TYPE_NULL);
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
        else {
            super.onBackPressed();
        }
    }

    @OnClick({R.id.autocomplete_search})
    public void openSearchPage() {
        pushFragment();
        showSearchButtons();
        autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    void pushFragment() {
        if (mFragmentUtils.getBackStackCount() < 1) {
            collapseBottomSheet();
            mFragmentUtils.replace(searchFragment, true);
        }
    }

    /*--------------E: map control section------------------*/
    @Subscribe
    public void onShowInMapClick(ShowInMapEvent e) {
        Place place = e.getPlace();
        hideKeyboard();
        showLocation(place.getPlaceLatLng());
        if (!mPresenter.checkHasPlace(mPresenter.getPlaceList(), place))
            pinLocation(place.getPlaceLatLng(), place.getPlaceTitle());
        mFragmentUtils.pop();
    }

    public void showAllLocation() {
        if (mPresenter.getPlaceList() != null && mPresenter.getNumPlace() > 1) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Place place : mPresenter.getPlaceList()) {
                builder.include(place.getPlaceLatLng());
            }
            LatLngBounds bounds;
            bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 180));

            showPlaceState = STATE_SHOW_ALL_LOCATION;
            updateShowLocationButton();
        }else{
            showCurrentLocation();
        }
    }

    public void showCurrentLocation() {
        if (mPresenter.getPlaceList() != null && mPresenter.getPlaceList().size() > 1) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mPresenter.getPlaceList().get(0).getPlaceLatLng());
            builder.include(mPresenter.excludeCurrentPlace(true).get(0).getPlaceLatLng());
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));

            showPlaceState = STATE_SHOW_CURRENT_LOCATION;
            updateShowLocationButton();
        }else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPresenter.getPlaceList().get(0).getPlaceLatLng(),17));

            showPlaceState = STATE_SHOW_CURRENT_LOCATION;
            updateShowLocationButton();
        }
    }

    public void showLocation(LatLng latLng) {
        float zoom = mMap.getCameraPosition().zoom;
        if (zoom < 10)
            zoom = 16;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void pinLocation(LatLng latLng, String placeTitle) {
        mMap.addMarker(new MarkerOptions().title(placeTitle).position(latLng));
    }

    public void pinLocation(LatLng latLng, String placeTitle, int num) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(placeTitle).icon(BitmapDescriptorFactory.fromBitmap(PinUtils.createNumberPin(this, num))));
    }

    public void pinAllLocation() {
        List<Place> placeList = mPresenter.excludeCurrentPlace(true);
        for (int i = placeList.size() - 1; i >= 0; i--) {
            Place place = placeList.get(i);
            pinLocation(place.getPlaceLatLng(), place.getPlaceTitle(), i);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        switch (screenState) {
            case STATE_SHOWTOOL:
                if (mFragmentUtils.getBackStackCount() == 0) {
                    hideMapTools();
                }
                break;
            case STATE_HIDETOOL:
                collapseBottomSheet();
                break;
        }
    }

    public void clearLine() {
        mMap.clear();
        pinAllLocation();
    }

    private void drawPath(int i) {
        if (i < mLineList.size()) {
            mMap.addPolyline(mLineList.get(i));
        }
    }

    private void drawAllPath() {
        for (int i = 1; i < mLineList.size(); i++) {
            PolylineOptions line = mLineList.get(i);
            mMap.addPolyline(line);
        }
        if (mLineList.size() > 0)
            mMap.addPolyline(mLineList.get(0));
    }

    public void showCurrentLine() {
        clearLine();
        drawPath(0);
        showLineState = STATE_SHOW_LINE;
        updateShowLineButton();
    }

    public void showAllLine() {
        clearLine();
        drawAllPath();
        showLineState = STATE_SHOW_ALL_LINE;
        updateShowLineButton();
    }

    /*------------------F: Map Tools Section------------------*/
    public void hideMapTools() {
        hideBottomSheet();
        hideSearchBar();
        screenState = STATE_HIDETOOL;
    }

    @OnClick(R.id.btn_show_location)
    void handleShowLocation() {
        switch (showPlaceState) {
            case STATE_SHOW_CURRENT_LOCATION:
                showAllLocation();
                break;
            case STATE_SHOW_ALL_LOCATION:
                showCurrentLocation();
                break;
        }
    }

    private void updateShowLocationButton() {
        switch (showPlaceState) {
            case STATE_SHOW_CURRENT_LOCATION:
                btnShowLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_location));
                break;
            case STATE_SHOW_ALL_LOCATION:
                btnShowLocation.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin));
                break;
        }
    }

    @OnClick(R.id.btn_show_line)
    void handleShowLine() {
        switch (showLineState) {
            case STATE_SHOW_LINE:
                showAllLine();
                break;
            case STATE_SHOW_ALL_LINE:
                showCurrentLine();
                break;
        }
    }

    private void updateShowLineButton() {
        if (mLineList.size() == 0)
            showLineState = STATE_SHOW_NO_LINE;
        switch (showLineState) {
            case STATE_SHOW_NO_LINE:
                btnShowLine.setImageDrawable(getResources().getDrawable(R.drawable.ic_show_line_inactive));
                btnShowLine.setEnabled(false);
                break;
            case STATE_SHOW_LINE:
                btnShowLine.setImageDrawable(getResources().getDrawable(R.drawable.ic_show_line_active));
                btnShowLine.setEnabled(true);
                break;
            case STATE_SHOW_ALL_LINE:
                btnShowLine.setImageDrawable(getResources().getDrawable(R.drawable.ic_show_lines));
                btnShowLine.setEnabled(true);
                break;
        }
    }


    /*--------------G: place list manage section---------------*/
    @Subscribe
    public void onAddPlace(AddPlaceEvent e) {
        mPresenter.addPlace(e.getPlace());
    }

    @Subscribe
    public void onRemovePlace(RemovePlaceEvent e) {
        mPresenter.removePlace(e.getPosition());
    }

    @Subscribe
    public void onSwapPlace(SwapPlaceEvent e){
        Log.d(TAG, "onSwapPlace: ");
        mPresenter.swapPlace(e.getFromPosition(),e.getToPosition());
    }

    @Override
    public void showToast(String str) {
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayAddLocation(String placeTitle) {
        showToast(getString(R.string.text_added,placeTitle));
    }

    public void removeAll() {
        mPresenter.removeAll();
    }

    /*---------------------Location Manage Section-------------*/

    /*-----------------------SQLite Database Section----------------*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*---------------------Other Section-----------------------*/

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

    @Override
    public void onStartTask() {
        //test
        TestUtils.getInstance().setStartTime();

        isTaskRunning = true;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        travelFragment.showLoadingView();
    }

    @Override
    public void onUpdateValue(int val) {
        progressBar.setProgress(val);
    }

    @Override
    public void onCancel() {
        showToast(getString(R.string.task_cancel));

        progressBar.setProgress(0);

        isTaskRunning = false;
        travelFragment.updateView();

        if(changeAlgorithm){
            mPresenter.calculatePath();
            changeAlgorithm = false;
        }else{
            PrefsUtil.setAlgorithm(this,prevAlgorithm);
        }
    }

    @Override
    public void onFinishCalculatePath() {
        //test
        long runtime = TestUtils.getInstance().getRuntime();

        progressBar.setProgress(0);

        mMap.clear();
        updateShowLineButton();
        updateShowLocationButton();
        pinAllLocation();

        isTaskRunning = false;

        travelFragment.setPlaceList(mPresenter.excludeCurrentPlace(true));
        travelFragment.updateDistance(mPresenter.getDistances(), mPresenter.getSumDistance());
        travelFragment.updateDuration(mPresenter.getDurations(), mPresenter.getSumDuration());
        travelFragment.updateView();

        prevAlgorithm = PrefsUtil.getAlgorithm(this);

        if(mPresenter.getNumPlace() > 0)
            TestUtils.getInstance().updateResultTable(mPresenter.getNumPlace(),toDistanceText(mPresenter.getSumDistance()),toDurationText(mPresenter.getSumDuration()),runtime);
    }

    @Override
    public void onFinishDrawPath(List<PolylineOptions> polylineOptions) {
        travelFragment.updateView();
        if (polylineOptions.size() > 0) {
            polylineOptions.get(0).color(getResources().getColor(R.color.activeTint));
            mLineList = polylineOptions;
            showAllLine();
        } else {
            mLineList.clear();
            updateShowLineButton();
        }
    }

    public String toDistanceText(int distance) {
        if (distance < 1000)
            return distance + " " + getString(R.string.m);
        else
            return Math.round(distance / 1000f) + " " + getString(R.string.km);
    }

    public String toDurationText(int duration) {
        int hours = Math.round(duration / 3600);
        int minutes = Math.round(duration / 60);
        String durationText = "";
        if (hours > 0) {
            durationText = hours + " " + getString(R.string.hr) + " ";
            minutes = Math.round(duration % 3600 / 60);
        }
        durationText = durationText + minutes + " " + getString(R.string.min);
        return durationText;
    }


    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public void onChangeAlgorithm() {
        if(!isTaskRunning){
            Log.d(TAG, "onChangeAlgorithm: ");
            mPresenter.calculatePath();
        }else{
            changeAlgorithm = true;
            mPresenter.cancelTask();
        }
    }

    public boolean isTaskRunning(){
        return isTaskRunning;
    }

    public void cancelTask(){
        changeAlgorithm = false;
        mPresenter.cancelTask();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
