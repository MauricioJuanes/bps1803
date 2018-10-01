package com.prototipo.prototipo.prototipo;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.prototipo.prototipo.prototipo.DataPersistence.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShowImageActivity extends AppCompatActivity {
    private ImageView imagen;
    private ImageButton boton_eliminar;
    private Database database;
    public String clave_historico = "clave_historico";
    public String clave_ultimo_archivo = "clave_ultimo_archivo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getSupportActionBar().setTitle("Histórico capturado");
        database = new Database(this.getApplicationContext());
        imagen = findViewById(R.id.imagen);
        boton_eliminar = findViewById(R.id.boton_eliminar);

        boton_eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDilog();
            }
        });

        Bundle bundle = getIntent().getExtras();
        String archivo = bundle.getString("imagen");

        Bitmap bitmap = null;
        File f = new File(archivo);
        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);

            ExifInterface exif = new ExifInterface(archivo);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagen.setImageBitmap(bitmap);


    }


    public void openDilog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("¿Esta seguro de eliminar la información del histórico CFE?");
                alertDialogBuilder.setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                database.DeleteElement(clave_historico);
                                database.DeleteElement(clave_ultimo_archivo);
                                Toast.makeText(ShowImageActivity.this,"Histórico eliminado", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
