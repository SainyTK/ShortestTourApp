package com.shortesttour.ui.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.ui.main.MainActivity;
import com.shortesttour.utils.JSONFileParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    @BindView(R.id.recycler_view_place_list)
    RecyclerView recyclerView;

    private List<Place> currentPlaceList;
    private List<Place> placeList;
    private PlaceExpandableAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_search,container,false);
        ButterKnife.bind(this,root);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        placeList = createPlaceList();
        currentPlaceList = placeList;

        adapter = new PlaceExpandableAdapter(getContext(), currentPlaceList);
        adapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onParentExpanded(int parentPosition) {
                adapter.collapseAllParents();
                adapter.expandParent(parentPosition);
            }

            @Override
            public void onParentCollapsed(int parentPosition) {

            }
        });
        recyclerView.setAdapter(adapter);

        return root;
    }

    public void setSearchDataSet(List<Place> dataSet){
        currentPlaceList = dataSet;
        adapter.modifyData(currentPlaceList);
    }

    public List<Place> getPlaceList(){
        return  placeList;
    }

    public List<Place> createPlaceList(){
        List<Place> places = null;
        try{
            places = JSONFileParser.getPlaces(getActivity(),getContext().getResources().getString(R.string.node_file_name));
        }catch (NullPointerException e){
            Log.e(TAG, "createPlaceList: NULL", e);
        }
        return places;
    }

}
