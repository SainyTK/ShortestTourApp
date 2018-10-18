package com.shortesttour.ui.travel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.ui.main.PlaceListItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TravelFragment extends Fragment implements PlaceListItemClickListener {

    @BindView(R.id.text_num_place)
    TextView textNumPlace;
    @BindView(R.id.text_going_to)
    TextView textGoingTo;
    @BindView(R.id.btn_add)
    TextView btnAdd;
    @BindView(R.id.text_total_time)
    TextView textTotalDuration;
    @BindView(R.id.text_total_distance)
    TextView textTotalDistance;
    @BindView(R.id.text_loading)
    TextView textLoading;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    
    private TravelListAdapter adapter;
    
    private List<Place> placeList;
    private int sumDuration;
    private int sumDistance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_travel,container,false);
        ButterKnife.bind(this,root);

        adapter = new TravelListAdapter(placeList);
        adapter.setListener(this);
        recyclerView.setLayoutManager(new TravelListLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);

        return root;
    }
    
    public void setPlaceList(List<Place> placeList){
        this.placeList = placeList;
        adapter.setData(placeList);
    }


    @Override
    public void onRemovePlace(int position) {

    }
}
