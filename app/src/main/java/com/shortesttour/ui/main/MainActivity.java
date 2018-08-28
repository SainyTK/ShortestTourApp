package com.shortesttour.ui.main;

import android.accessibilityservice.AccessibilityService;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.ui.map.MapFragment;
import com.shortesttour.ui.search.PlaceParent;
import com.shortesttour.ui.search.SearchAdapter;
import com.shortesttour.ui.search.SearchFragment;
import com.shortesttour.ui.search.SearchOptionSelectedListener;
import com.shortesttour.utils.FragmentUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SearchOptionSelectedListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.autocomplete_search)
    EditText autoCompleteTextView;
    @BindView(R.id.search_back_btn)
    ImageView searchBackButton;
    @BindView(R.id.search_clear_btn)
    ImageView searchClearButton;

    private FragmentUtils mFragmentUtils;

    private Fragment currentFragment;

    private MapFragment mapFragment;
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

        setupUI();

        mFragmentUtils = new FragmentUtils(this,R.id.fragment_container);

        mapFragment = MapFragment.newInstance();
        searchFragment = SearchFragment.newInstance();

        searchFragment.setOptionSelectedListener(this);

        mFragmentUtils.replace(mapFragment,true);
    }

    private void setupUI(){

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

    @OnClick(R.id.search_back_btn)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: count = " + mFragmentUtils.getBackStackCount());
        hideSearchButtons();
        clearSearchBox();
        if(mFragmentUtils.getBackStackCount()==0)
            finish();
    }

    private void showSearchButtons(){
        searchBackButton.setVisibility(View.VISIBLE);
        searchClearButton.setVisibility(View.VISIBLE);
    }

    private void hideSearchButtons(){
        searchBackButton.setVisibility(View.GONE);
        searchClearButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.autocomplete_search)
    void pushFragment(){
        if(mFragmentUtils.getBackStackCount()<2){
            showSearchButtons();
            mFragmentUtils.add(searchFragment);
        }
    }

    @OnClick(R.id.search_clear_btn)
    void clearSearchBox(){
        autoCompleteTextView.setText("");
    }


    @Override
    public void showInMap(LatLng latLng, String placeTitle) {
        Log.d(TAG, "showInMap: ");
        if(mapFragment!=null){
            mapFragment.showLocation(latLng,placeTitle);
            onBackPressed();
        }
    }
}
