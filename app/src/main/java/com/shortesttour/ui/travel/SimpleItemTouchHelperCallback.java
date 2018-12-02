package com.shortesttour.ui.travel;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private static final String TAG = "SimpleItemTouchHelperCa";

    private final ItemTouchHelperAdapter mAdapter;

    private int startPos;
    private int stopPos;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT;
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//        Log.d(TAG, "onMove: from " + viewHolder.getAdapterPosition() + " to " + target.getAdapterPosition());
        mAdapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((TravelListAdapter.Holder) viewHolder).getViewForeGround();
            getDefaultUIUtil().onSelected(foregroundView);
        }
        if(actionState == ItemTouchHelper.ACTION_STATE_DRAG){
            if(viewHolder != null)
                startPos = viewHolder.getAdapterPosition();
        }

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
            startPos = -1;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((TravelListAdapter.Holder) viewHolder).getViewForeGround();
        getDefaultUIUtil().clearView(foregroundView);
        if(startPos > 0){
            stopPos = viewHolder.getAdapterPosition();
            mAdapter.onItemMoved(startPos,stopPos);
        }

    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(actionState == 1){
            final View foregroundView = ((TravelListAdapter.Holder) viewHolder).getViewForeGround();
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
        }
        else {
            super.onChildDrawOver(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
        }
    }


    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if(actionState == 1){
            final View foregroundView = ((TravelListAdapter.Holder) viewHolder).getViewForeGround();
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
        }
        else{
            super.onChildDraw(c,recyclerView,viewHolder,0,dY,actionState,isCurrentlyActive);
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

}
