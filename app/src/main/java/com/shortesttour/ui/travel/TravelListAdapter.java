package com.shortesttour.ui.travel;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shortesttour.R;
import com.shortesttour.events.RemovePlaceEvent;
import com.shortesttour.events.SwapPlaceEvent;
import com.shortesttour.models.Place;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TravelListAdapter extends RecyclerView.Adapter<TravelListAdapter.Holder> implements ItemTouchHelperAdapter{

    private static final String TAG = "TravelListAdapter";

    public List<Place> mPlaceList;

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_place_title)
        TextView textPlaceTitle;
        @BindView(R.id.place_distance)
        TextView textPlaceDistance;
        @BindView(R.id.place_icon)
        TextView textPlaceIcon;
        @BindView(R.id.view_background)
        View viewBackGround;
        @BindView(R.id.view_foreground)
        View viewForeGround;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void bindData(int position){
            textPlaceTitle.setText(mPlaceList.get(position).getPlaceTitle());
            textPlaceDistance.setText(mPlaceList.get(position).getDistanceText());
            textPlaceIcon.setText(String.valueOf(position+1));
        }

        public View getViewBackGround(){
            return viewBackGround;
        }

        public View getViewForeGround(){
            return viewForeGround;
        }

    }

    public TravelListAdapter(List<Place> placeList){
        mPlaceList = placeList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_bottomsheet_place_list,parent,false);
        return new Holder(root);
    }

    @Override
    synchronized public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{
            holder.bindData(position);
        }catch(Exception e){
            Log.e("Error", "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        try{
            return mPlaceList.size();
        }catch (Exception e){
            Log.e(TAG, "getItemCount: ", e);
        }
        return 0;
    }

    public void setData(List<Place> placeList){
        mPlaceList = placeList;
        notifyDataSetChanged();
    }

    public void addPlace(Place place){
        place.setOrder(getItemCount());
        mPlaceList.add(place);
        notifyItemChanged(getItemCount()-1);
    }

    public void removePlace(int position){
        mPlaceList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateDistance(int position, int value){
        mPlaceList.get(position).setDistance(value);
        notifyItemChanged(position);
    }

    public List<Place> getData(){
        return mPlaceList;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if(fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mPlaceList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition ; i--) {
                Collections.swap(mPlaceList,i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        EventBus.getDefault().post(new SwapPlaceEvent(fromPosition,toPosition));
    }

    @Override
    public void onItemDismiss(int position) {
        removePlace(position);
        EventBus.getDefault().post(new RemovePlaceEvent(position));
    }

}
