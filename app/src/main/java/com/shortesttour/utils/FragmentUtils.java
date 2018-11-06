package com.shortesttour.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;

public class FragmentUtils {

    private final AppCompatActivity activity;
    private final int fragmentContainerId;

    public FragmentUtils(AppCompatActivity activity,int fragmentContainerId){
        this.activity = activity;
        this.fragmentContainerId = fragmentContainerId;
    }

    public void replace(@NonNull Fragment fragment,boolean addBackStack){
        String backStateName = fragment.getClass().getName();

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.replace(fragmentContainerId,fragment);
        if(addBackStack)
            ft.addToBackStack(backStateName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void add(@NonNull Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(fragmentContainerId, fragment);
        ft.addToBackStack(backStateName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void pop(){
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.popBackStack();
    }

    public int getBackStackCount(){
        return activity.getSupportFragmentManager().getBackStackEntryCount();
    }
}
