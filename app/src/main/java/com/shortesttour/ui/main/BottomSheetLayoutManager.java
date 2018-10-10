package com.shortesttour.ui.main;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class BottomSheetLayoutManager extends LinearLayoutManager {
    private static final String TAG = "BottomSheetLayoutManage";

    public BottomSheetLayoutManager(Context context) {
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
