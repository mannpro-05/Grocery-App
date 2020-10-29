package com.guni.uvpce.ceit.food.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {

    private static Boolean mIsNightMode = false;
    private static final String PREF_DARK_THEME = "DarkTheme";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loadSettings();
        setNightMode();
    }
    public static boolean getNightMode()
    {
        return mIsNightMode;
    }
    public void loadSettings()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mIsNightMode = pref.getBoolean(PREF_DARK_THEME,true);
    }
    public static void setNightMode(Context context, boolean isNightMode)
    {
        mIsNightMode = isNightMode;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor;
        prefsEditor = pref.edit();
        prefsEditor.putBoolean(PREF_DARK_THEME,mIsNightMode);
        prefsEditor.apply();
    }

    public void setNightMode()
    {
        if(mIsNightMode){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
