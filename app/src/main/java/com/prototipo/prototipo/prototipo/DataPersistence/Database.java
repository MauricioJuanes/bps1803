package com.prototipo.prototipo.prototipo.DataPersistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.location.Location;
import android.location.LocationListener;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Context context;
    private SharedPreferences db;


    public Database(Context context){
        this.context = context;
        this.db = context.getSharedPreferences("ApplicationData",Context.MODE_PRIVATE );
    }

    public void saveElement(String name, String value){
        Editor editor = db.edit();
        editor.putString(name, value );
        editor.apply();
    }

    public String getElement(String name){
        return db.getString(name, "");
    }

    public void saveCalculatedArea(double localArea){
        Editor editor = db.edit();
        editor.putString("calculatedArea", String.valueOf(localArea) );
        editor.apply();
    }

    public void resetCalculatedArea(){
        Editor editor = db.edit();
        editor.putString("calculatedArea", String.valueOf(0.0) );
        editor.apply();
    }

    public double  getCalculatedArea(){
        double calculatedArea = 0;
        calculatedArea = Double.parseDouble(db.getString("calculatedArea", "0.0"));
        return calculatedArea;
    }

    public void saveCurrentPosition(String latLng){
        Editor editor = db.edit();
        editor.putString("lastPosition", latLng);
        editor.apply();
    }

    public LatLng getLastPosition(){
        String[] latLng = db.getString("lastPosition", "0.0,0.0").split(",");
        double latitude = Double.parseDouble(latLng[0]);
        double longitude = Double.parseDouble(latLng[1]);
        LatLng location = new LatLng(latitude, longitude);
        return location;
    }

    public void saveAreaMarkers(ArrayList<LatLng> positionMarkersOnMap){
        Editor editor = db.edit();
        editor.putString("positionMarkersOnMap", new Gson().toJson(positionMarkersOnMap));
        editor.apply();
    }

    public void resetAreaMarkers(){
        Editor editor = db.edit();
        editor.putString("positionMarkersOnMap", " ");
        editor.apply();
    }

    public List<LatLng> getAreaMarkers(){
        List<LatLng>  positionsArray = new ArrayList<>();
        String positions = db.getString("positionMarkersOnMap", " ");
        try{
            Type type = new TypeToken<List<LatLng>>(){}.getType();
            positionsArray = new Gson().fromJson(positions, type);
        }catch (Exception e){/**/}

        return positionsArray;
    }

    public String getRawAreaMarkers(){
        String positions = db.getString("positionMarkersOnMap", "");

        return positions;
    }

    public void DeleteElement(String name){
        Editor editor = db.edit();
        editor.remove(name);
        editor.apply();
    }

    public void saveHistoric(String historic){
        Editor editor = db.edit();
        editor.putString("historico",historic);
        editor.apply();
    }

    public String getHistoric(){
        return db.getString("historico","");
    }


}
