package com.prototipo.prototipo.prototipo;

import android.graphics.Bitmap;

public class Questionnaire {
    private String name;
    private String owner;
    private String extraDoors;
    private String authorization;
    private String roofArea;
    private String roofCorners;
    private String history;
    private Bitmap energyConsumption;
    private Bitmap idFront;
    private Bitmap idBack;


    public Questionnaire(String name, String owner, String extraDoors, String authorization, String roofArea, String roofCorners, String history, Bitmap energyConsumption, Bitmap idFront, Bitmap idBack) {
        this.name = name;
        this.owner = owner;
        this.extraDoors = extraDoors;
        this.authorization = authorization;
        this.roofArea = roofArea;
        this.roofCorners = roofCorners;
        this.history = history;
        this.energyConsumption = energyConsumption;
        this.idFront = idFront;
        this.idBack = idBack;
    }

}
