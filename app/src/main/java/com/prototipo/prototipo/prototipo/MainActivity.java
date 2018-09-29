package com.prototipo.prototipo.prototipo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //List of options for the main view
    private List<CustomItemList> optionList;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing objects
        optionList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.options_list_view);

        //adding some values to our list
        optionList.add(new CustomItemList("Área de un local", "000 m2"));
        optionList.add(new CustomItemList("Histórico CFE", "Captura los datos de tu recibo"));

        //creating the adapter
        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.item_list_layout, optionList);

        //attaching adapter to the listview
        listView.setAdapter(adapter);
    }
}
