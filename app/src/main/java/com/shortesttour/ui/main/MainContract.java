package com.shortesttour.ui.main;

public class MainContract {
    public interface View{
        void showData(String data);
    }

    public interface Presenter{
        void loadData();
    }
}
