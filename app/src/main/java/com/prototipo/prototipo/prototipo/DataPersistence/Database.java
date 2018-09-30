package com.prototipo.prototipo.prototipo.DataPersistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;

public class Database {
    private Context context;
    private SharedPreferences db;


    public Database(Context context){
        this.context = context;
        this.db = context.getSharedPreferences("ApplicationData",Context.MODE_PRIVATE );
    }

    public String saveElement(String name, String value){
        return "";
    }

    public Boolean existsElement(String name){
        return true;
    }

    public void saveCalculatedArea(double localArea){
        Editor editor = db.edit();
        editor.putString("calculatedArea", String.valueOf(localArea) );
        editor.apply();
    }

    public double  getCalculatedArea(){
        double calculatedArea = 0;
        calculatedArea = Double.parseDouble(db.getString("calculatedArea", "0.0"));
        return calculatedArea;
    }



}
