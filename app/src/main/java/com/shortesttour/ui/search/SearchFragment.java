package com.shortesttour.ui.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.R;
import com.shortesttour.models.Place;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    @BindView(R.id.recycler_view_place_list)
    RecyclerView recyclerView;

    List<PlaceParent> currentPlaceList;
    List<PlaceParent> placeList;
    PlaceExpandableAdapter adapter;

    SearchOptionSelectedListener mListener;

    public static SearchFragment newInstance(){
        SearchFragment searchFragment = new SearchFragment();
        return searchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_search,container,false);
        ButterKnife.bind(this,root);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        placeList = createPlaceTitleList();
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
        adapter.setOptionSelectedListener(mListener);
        recyclerView.setAdapter(adapter);

        return root;
    }

    public void setSearchDataSet(List<PlaceParent> dataSet){
        currentPlaceList = dataSet;
        adapter.modifyData(currentPlaceList);
    }

    public List<PlaceParent> getPlaceList(){
        return  placeList;
    }

    private List<PlaceParent> createPlaceTitleList(){
        List<PlaceParent> placeParents = new ArrayList<>();

        placeParents.add(toPlaceParent(new Place("ภูเก็ต",new LatLng(7.957630,98.337373))));
        placeParents.add(toPlaceParent(new Place("บางคู",new LatLng(7.955125,98.379756))));
        placeParents.add(toPlaceParent(new Place("ป่าคลอก",new LatLng(7.963541,98.385762))));
        placeParents.add(toPlaceParent(new Place("สะปำ",new LatLng(7.939059,98.395983))));
        placeParents.add(toPlaceParent(new Place("อำเภอกะทู้",new LatLng(7.908874,98.333839))));
        placeParents.add(toPlaceParent(new Place("ม.อ. ภูเก็ต",new LatLng(7.893656,98.352670))));
        placeParents.add(toPlaceParent(new Place("ภูเก็ตคันทรี่คลับ",new LatLng(7.900542,98.345292))));
        placeParents.add(toPlaceParent(new Place("โรงเรียนขจรเกียรติ",new LatLng(7.908321,98.360563))));
        placeParents.add(toPlaceParent(new Place("ป่าตอง",new LatLng(7.892423,98.297194))));
        placeParents.add(toPlaceParent(new Place("กะรน",new LatLng(7.845788,98.296189))));

        return placeParents;
    }

    private PlaceParent toPlaceParent(Place place){
        return new PlaceParent(place);
    }

    public void setOptionSelectedListener(SearchOptionSelectedListener listener){
        mListener = listener;
    }


}
