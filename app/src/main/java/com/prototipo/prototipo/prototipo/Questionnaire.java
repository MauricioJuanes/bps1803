package com.prototipo.prototipo.prototipo;

import android.graphics.Bitmap;

public class Questionnaire {
    private String name;
    private String isOwner;
    private String extraDoors;
    private String isBureauAuthorized;
    private String roofArea;
    private String roofCorners;
    private String history;
    private Bitmap energyConsumption;
    private Bitmap idFront;
    private Bitmap idBack;


    public Questionnaire(String name, String isOwner, String extraDoors, String isBureauAuthorized, String roofArea, String roofCorners, String history, Bitmap energyConsumption, Bitmap idFront, Bitmap idBack) {
        this.name = name;
        this.isOwner = isOwner;
        this.extraDoors = extraDoors;
        this.isBureauAuthorized = isBureauAuthorized;
        this.roofArea = roofArea;
        this.roofCorners = roofCorners;
        this.history = history;
        this.energyConsumption = energyConsumption;
        this.idFront = idFront;
        this.idBack = idBack;
    }

}
