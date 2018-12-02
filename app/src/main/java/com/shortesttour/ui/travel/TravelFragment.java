package com.shortesttour.ui.travel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.ui.main.MainActivity;

import java.util.ArrayList;
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
    @BindView(R.id.btn_cancel)
    TextView btnCancel;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private TravelListAdapter adapter;
    private MainActivity activity;

    private int sumDuration = 0;
    private int sumDistance = 0;

    public TravelFragment(){
        adapter = new TravelListAdapter(new ArrayList<Place>());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            View root = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_travel,container,false);
            ButterKnife.bind(this,root);
            activity = (MainActivity)getActivity();

            setupRecyclerView();

            updateView();
            return root;
        }catch (Exception e){
            Log.e("error", "onCreateView: ", e);
        }
        return null;
    }

    private void setupRecyclerView(){
        recyclerView.setLayoutManager(new TravelListLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("test", "onResume: ");
        adapter.setData(adapter.getData());
        updateView();
    }

    private void setButtonNoPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        btnAdd.setVisibility(View.VISIBLE);
        textNumPlace.setText(activity.getString(R.string.no_place));

        //gone
        textGoingTo.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        textTotalDuration.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    private void setButtonHasPlace(){
        //visible
        textNumPlace.setVisibility(View.VISIBLE);
        textGoingTo.setVisibility(View.VISIBLE);
        textTotalDistance.setVisibility(View.VISIBLE);
        textTotalDuration.setVisibility(View.VISIBLE);

        //gone
        btnAdd.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    public void showLoadingView(){
        //visible
        textNumPlace.setText(activity.getString(R.string.loading));
        textNumPlace.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);

        //gone
        btnAdd.setVisibility(View.GONE);
        textGoingTo.setVisibility(View.GONE);
        textTotalDistance.setVisibility(View.GONE);
        textTotalDuration.setVisibility(View.GONE);

    }

    public void updateView(){
        List<Place> placeList = adapter.getData();
        if(placeList.size()>0){
            if(activity.isTaskRunning())
                showLoadingView();
            else{
                setButtonHasPlace();

                String strNumPlace = activity.getString(R.string.places,placeList.size());
                textNumPlace.setText(strNumPlace);

                String strGoingTo = activity.getString(R.string.going_to,placeList.get(0).getPlaceTitle());
                textGoingTo.setText(strGoingTo);

                String strTotalDistance = activity.getString(R.string.total_distance,toDistanceText(sumDistance));
                textTotalDistance.setText(strTotalDistance);

                String strTotalDuration = activity.getString(R.string.total_duration,toDurationText(sumDuration));
                textTotalDuration.setText(strTotalDuration);
            }
        }else{
            setButtonNoPlace();
        }
    }

    public String toDistanceText(int distance){
        if(distance<1000)
            return distance + " " + activity.getString(R.string.m);
        else
            return Math.round(distance/1000f) + " " + activity.getString(R.string.km);
    }

    public String toDurationText(int duration){
        Log.d("SHOW", "toDurationText: " + duration);
        int hours = Math.round(duration/3600);
        int minutes = Math.round(duration/60);
        String durationText = "";
        if(hours>0){
            durationText = hours + " " + activity.getString(R.string.hr) + " ";
            minutes = Math.round(duration%3600/60);
        }
        durationText = durationText + minutes + " " + activity.getString(R.string.min);
        return durationText;
    }

    public void updateDistance(int[] pathValues,int sumDistance){
        int sum = 0;

        for(int i=0;i<adapter.getData().size();i++){
            sum+=pathValues[i];
            adapter.updateDistance(i,sum);
        }

        this.sumDistance = sumDistance;
    }

    public void updateDuration(int[] pathValues,int sumDuration){
        int sum = 0;

        for(int i=0;i<adapter.getData().size();i++){
            sum+=pathValues[i];
        }

        this.sumDuration = sumDuration;
    }

    public void setPlaceList(List<Place> placeList){
        adapter.setData(placeList);

    }

    @OnClick(R.id.btn_add)
    void addPlace(){
        activity.openSearchPage();
    }

    @OnClick(R.id.btn_cancel)
    void cancelTask(){
        activity.cancelTask();
    }
}
