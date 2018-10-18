package com.shortesttour.ui.travel;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class TravelListLayoutManager extends LinearLayoutManager {
    private static final String TAG = "BottomSheetLayoutManage";

    public TravelListLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try{
            super.onLayoutChildren(recycler, state);
        }catch (IndexOutOfBoundsException ie){
            Log.e(TAG, "onLayoutChildren: ", ie);
        }

    }
}
