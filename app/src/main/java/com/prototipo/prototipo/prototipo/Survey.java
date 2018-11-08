package com.prototipo.prototipo.prototipo;

import android.net.Uri;

public class Survey {
    public String name;
    private String moreInfo;
    private String isOwner;
    private String extraDoors;
    private String isBureauAuthorized;
    private String roofArea;
    private String roofCorners;
    private Uri history;
    private Uri energyConsumption;
    private Uri idFront;
    private Uri idBack;


    public Survey(String name, String moreInfo, String isOwner, String extraDoors, String isBureauAuthorized, String roofArea, String roofCorners, Uri history, Uri energyConsumption, Uri idFront, Uri idBack) {
        this.name = name;
        this.moreInfo = moreInfo;
        this.isOwner = isOwner;
        this.extraDoors = extraDoors;
        this.isBureauAuthorized = isBureauAuthorized;
        this.roofArea = roofArea;
        this.roofCorners = roofCorners;
        this.history = history;
        this.energyConsumption = energyConsumption;
        this.idFront = idFront;
        this.idBack = idBack;
        System.out.println(history);
        System.out.println(energyConsumption);
        System.out.println(idFront);
        System.out.println(idBack);
    }

    @Override
    public String toString(){
        return "Nombre del cliente: " + name + "\n" +
               "¿Desea obtener mas informacion?: " + moreInfo + "\n" +
               "¿Es Propietario?: " + isOwner + "\n" +
               "¿Puertas extra?: " + extraDoors + "\n" +
               "¿Autoriza el buró?: " + isBureauAuthorized + "\n" +
               "Area del Tejado: " + roofArea + " m2" + "\n" +
               "Esquinas del tejado: " + roofCorners;
    }



}
