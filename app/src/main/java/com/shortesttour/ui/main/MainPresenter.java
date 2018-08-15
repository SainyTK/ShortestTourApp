package com.shortesttour.ui.main;

import javax.inject.Inject;

public class MainPresenter implements MainContract.Presenter{

    MainContract.View mView;

    @Inject
    public MainPresenter(MainContract.View view){
        this.mView = view;
    }

    @Override
    public void loadData() {
        mView.showData("Good Morning");
    }

}
