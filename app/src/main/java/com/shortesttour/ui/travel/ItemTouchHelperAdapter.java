package com.shortesttour.ui.travel;

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition,int toPosition);
    void onItemDismiss(int position);
    void onItemMoved(int fromPosition, int toPosition);
}
