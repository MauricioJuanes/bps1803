package com.prototipo.prototipo.prototipo;

import android.widget.Button;

public class CustomItemList {
    //Data model for item list options
    private String title;
    private String description;
    private Button button;

    CustomItemList(String title, String description){
        this.title = title;
        this.description =  description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
}
