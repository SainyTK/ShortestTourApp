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
import com.shortesttour.ui.main.MainActivity;
import com.shortesttour.ui.main.PlaceListItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TravelFragment extends Fragment{

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
    private MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_travel,container,false);
        ButterKnife.bind(this,root);
        activity = (MainActivity)getActivity();

        adapter = new TravelListAdapter(placeList);
        recyclerView.setLayoutManager(new TravelListLayoutManager(container.getContext()));
        recyclerView.setAdapter(adapter);

        setButtonNoPlace();

        return root;
    }

    private void setButtonNoPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        btnAdd.setVisibility(View.VISIBLE);
        textNumPlace.setText(getResources().getString(R.string.no_place));

        //gone
        textGoingTo.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        textTotalDuration.setVisibility(View.GONE);
        textLoading.setVisibility(View.GONE);
    }

    private void setButtonHasPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        textGoingTo.setVisibility(View.VISIBLE);
        textTotalDistance.setVisibility(View.VISIBLE);
        textTotalDuration.setVisibility(View.VISIBLE);

        //gone
        btnAdd.setVisibility(View.GONE);
        textLoading.setVisibility(View.GONE);
    }

    public void updateView(){
        if(placeList.size()>0){
            setButtonHasPlace();

            String strNumPlace = getString(R.string.places,placeList.size());
            textNumPlace.setText(strNumPlace);

            String strGoingTo = getString(R.string.going_to,placeList.get(0).getPlaceTitle());
            textGoingTo.setText(strGoingTo);

            String strTotalDistance = getString(R.string.total_distance,toDistanceText(activity.getSumDistance()));
            textTotalDistance.setText(strTotalDistance);

            String strTotalDuration = getString(R.string.total_duration,toDurationText(activity.getSumDuration()));
            textTotalDuration.setText(strTotalDuration);
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
        int minutes = Math.round(duration%60);
        String durationText = "";
        if(hours>0){
            durationText = hours + " " + getString(R.string.hr) + " ";
            minutes = Math.round(duration/3600%60);
        }
        durationText = durationText + minutes + " " + getString(R.string.min);
        return durationText;
    }

    public void updateDistance(int[] pathValues){
        int sum = 0;

        for(int i=0;i<adapter.getData().size();i++){
            sum+=pathValues[i];
            adapter.updateDistance(i,sum);
        }
    }

    public void setListener(PlaceListItemClickListener listener){
        adapter.setListener(listener);
    }

    public void setPlaceList(List<Place> placeList){
        this.placeList = placeList;
        adapter.setData(placeList);
    }

    public List<Place> getPlaceList(){
        return placeList;
    }

    public void addPlace(Place place){
        adapter.addPlace(place);
    }

    @OnClick(R.id.btn_add)
    void addPlace(){
        activity.openSearchPage();
    }
}
