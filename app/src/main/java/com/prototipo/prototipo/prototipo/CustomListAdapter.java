package com.prototipo.prototipo.prototipo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<CustomItemList> {

    private List<CustomItemList> optionsList;
    private Context context;
    private int resource;

    //constructor initializing the values
    public CustomListAdapter(Context context, int resource, List<CustomItemList> optionsList) {
        super(context, resource, optionsList);
        this.context = context;
        this.resource = resource;
        this.optionsList = optionsList;
    }

    public View getView(final int position, @Nullable
            View convertView, @NonNull ViewGroup parent) {

        //we need to get the view of the xml for our list item
        //And for this we need a layoutinflater
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        //getting the view
        View view = layoutInflater.inflate(resource, null, false);

        //getting the view elements of the list from the view
        TextView title = view.findViewById(R.id.title_option);
        TextView description = view.findViewById(R.id.description_option);
        Button actionButton = view.findViewById(R.id.button_option);

        //getting the option of the specified position
        CustomItemList selectedOption = optionsList.get(position);

        //adding values to the list item option
        title.setText(selectedOption.getTitle());
        description.setText(selectedOption.getDescription());


        //adding a click listener to the button to remove item from the list
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //we are passing the position which is the selected item and execute a new task in the app
                showNewTask(position);
            }
        });

        //finally returning the view
        return view;
    }

    //this method will show the new activity for other tasks
    private void showNewTask(final int position) {

        switch (position){
            case 0:
                System.out.println("0 was selected");
                break;
            case 1:
                System.out.println("1 was selected");
                break;
            case 2:
                System.out.println("2 was selected");
                break;
                default:
                    System.out.println("Another was selected");
        }
    }


}
