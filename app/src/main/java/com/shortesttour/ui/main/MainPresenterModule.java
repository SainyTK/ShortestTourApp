package com.shortesttour.ui.main;


import dagger.Module;
import dagger.Provides;

@Module
public class MainPresenterModule {
    private final MainContract.View mView;

    public MainPresenterModule(MainContract.View view){
        mView = view;
    }

    @Provides
    public MainContract.View providesMainContractView(){
        return mView;
    }
}
