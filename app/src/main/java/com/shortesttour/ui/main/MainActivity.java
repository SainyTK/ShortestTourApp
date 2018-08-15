package com.shortesttour.ui.main;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.shortesttour.R;
import com.shortesttour.ui.Fragment1;
import com.shortesttour.ui.map.MapFragment;
import com.shortesttour.ui.search.SearchFragment;
import com.shortesttour.utils.FragmentUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.autocomplete_search)
    AutoCompleteTextView autoCompleteTextView;

    private FragmentUtils mFragmentUtils;

    private Fragment currentFragment;

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

        setupUI();

        mFragmentUtils = new FragmentUtils(this,R.id.fragment_container);

        currentFragment = MapFragment.newInstance();
        mFragmentUtils.replace(currentFragment,true);
    }

    private void setupUI(){
        String[] countries = getResources().getStringArray(R.array.countries_array);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,countries);
        autoCompleteTextView.setAdapter(arrayAdapter);
    }

    @OnClick(R.id.autocomplete_search)
    void pushFragment(){
        currentFragment = SearchFragment.newInstance();
        mFragmentUtils.add(currentFragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: count = " + mFragmentUtils.getBackStackCount());
        if(mFragmentUtils.getBackStackCount()==0)
            finish();
    }
}
