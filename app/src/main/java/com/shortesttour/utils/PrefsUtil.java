package com.shortesttour.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsUtil {
    private static final String ALGORITHM = "algorithm";

    public static final int NEAREST_NEIGHBOR = 0;
    public static final int DYNAMIC_PROGRAMMING = 1;

    private static SharedPreferences getPrefs(Context context, String prefName){
        return context.getSharedPreferences(prefName,0);
    }

    private static SharedPreferences.Editor getEditor(Context context,String prefName){
        return getPrefs(context,prefName).edit();
    }

    public static void setAlgorithm(Context context,int algorithm){
        getEditor(context,ALGORITHM).putInt(ALGORITHM,algorithm).apply();
    }

    public static int getAlgorithm(Context context){
        return getPrefs(context,ALGORITHM).getInt(ALGORITHM,0);
    }
}
