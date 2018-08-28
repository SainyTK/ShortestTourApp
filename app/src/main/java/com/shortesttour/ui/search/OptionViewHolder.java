package com.shortesttour.ui.search;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.shortesttour.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class OptionViewHolder extends ChildViewHolder {

    public interface OptionClickListener{
        void clickShowInMap(int parentPosition);
        void clickAddToList(int parentPosition);
    }

    private OptionClickListener mListener;

    public OptionViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void setOptionClickListener(OptionClickListener listener){
        mListener = listener;
    }

    @OnClick(R.id.btn_show_in_map)
    void showInMap(){
        if(mListener!=null)
            mListener.clickShowInMap(getParentAdapterPosition());
    }

    @OnClick(R.id.btn_add_to_list)
    void addToList(){
        if(mListener!=null)
            mListener.clickAddToList(getParentAdapterPosition());
    }
}
