package com.prototipo.prototipo.prototipo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.prototipo.prototipo.prototipo.CustomUserListView.CustomItemList;
import com.prototipo.prototipo.prototipo.CustomUserListView.CustomListAdapter;
import com.prototipo.prototipo.prototipo.DataPersistence.Database;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private  static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private ImageView imageView;
    private ImageButton boton_historico_cfe;
    private ImageButton boton_frente_recibo_cfe;
    private CheckBox checkbox_propietario;
    private Uri archivo;
    private Database database;


    //List of options for the main view
    private List<CustomItemList> optionList;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = new Database(this.getApplicationContext());
        boton_historico_cfe = findViewById(R.id.btn_historico_cfe);
        boton_frente_recibo_cfe = findViewById(R.id.btn_frente_recibo_cfe);
        checkbox_propietario = findViewById(R.id.chk_propietario);


        //initializing objects
        optionList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.options_list_view);
        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.item_list_layout, optionList);
        listView.setAdapter(adapter);

        //adding some values to our list
        optionList.add(new CustomItemList("Área de un local", new DecimalFormat("##.##").format(database.getCalculatedArea())+" m2"));
        optionList.add(new CustomItemList("Histórico CFE", "Captura los datos de tu recibo"));

        cambiarEstadoBoton(boton_historico_cfe, Boolean.FALSE);
        cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cambiarEstadoBoton(boton_historico_cfe, Boolean.FALSE);
            cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        checkbox_propietario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkbox_propietario.isChecked()){
                    cambiarEstadoBoton(boton_historico_cfe, Boolean.TRUE);
                    cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.TRUE);
                }
                else{
                    cambiarEstadoBoton(boton_historico_cfe, Boolean.FALSE);
                    cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);
                }
            }
        });

        boton_historico_cfe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFotografia();
            }
        });
    }

    private void cambiarEstadoBoton(ImageButton boton, Boolean estado){
        boton.setEnabled(estado);
        if(estado){
            boton.setBackgroundResource(R.drawable.round_button_active);
        }
        else {
            boton.setBackgroundResource(R.drawable.round_button_inactive);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permiso externo aceptado");
            }
        }
    }

    private void tomarFotografia(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        archivo = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, archivo);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                System.out.println("Imagen tomada");
            }
        }
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    @Override
    public void onResume(){
        super.onResume();
        // change this code to refresh all content for this view. Shows data saved on this device via shared preferences
        optionList = new ArrayList<>();
        listView = findViewById(R.id.options_list_view);
        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.item_list_layout, optionList);
        listView.setAdapter(adapter);

        //adding some values to our list edit if listview is removed
        optionList.add(new CustomItemList("Área de un local", new DecimalFormat("##.##").format(database.getCalculatedArea())+" m2"));
        optionList.add(new CustomItemList("Histórico CFE", "Captura los datos de tu recibo"));

    }
}
