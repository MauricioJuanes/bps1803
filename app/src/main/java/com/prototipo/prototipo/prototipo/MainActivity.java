package com.prototipo.prototipo.prototipo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import java.io.IOException;
import java.lang.ref.WeakReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prototipo.prototipo.prototipo.DataPersistence.Database;

import com.prototipo.prototipo.prototipo.Maps.MapActivity;
import com.prototipo.prototipo.prototipo.Models.Historico;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private boolean close = false;
    private boolean isHistoric = false;
    private  static final int GUARDAR_FOTO_HISTORICO = 1;
    private  static final int GUARDAR_FOTO_CONSUMO = 2;
    private  static final int GUARDAR_INE_FRENTE = 3;
    private  static final int GUARDAR_INE_ATRAS = 4;
    private Bitmap bitmap;
    private ImageView imageView;
    private ImageButton boton_historico_cfe;
    private ImageButton boton_frente_recibo_cfe;
    private ImageButton boton_mapa_area_local;
    private ImageButton boton_consumo_de_luz;
    private ImageButton boton_ine_frente;
    private ImageButton boton_ine_atras;
    private TextView texto_mapa_area_local_descripcion;

    private Spinner clientSpinner;
    private Spinner doorSpinner;
    private RadioGroup rdgMasInfo;
    private RadioGroup rdgPropietario;
    private RadioGroup rdgCredito;
    private Button btn_EnviarPorCorreo;
    private Button btn_LimpiarCampos;
    private Button btn_Salir;
//    private CheckBox checkbox_propietario;
    private Uri ruta_foto_historico;
    private String ultima_foto_Historico;
    public ArrayList<Historico> historico_cfe;

    private Database database;

    private String[] puertas;
    public static final int INDEX_ZERO = 0;


    private static final String CLOUD_VISION_API_KEY = "AIzaSyAQWr3is_y_UXhi8GQccoBAihO2NGQiSJk";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public String clave_historico = "clave_historico";
    public String clave_ultimo_archivo = "clave_ultimo_archivo";
    public Uri ultima_foto_ruta;

    private Gson gson;

    private Uri ruta_foto_Consumo;
    private String ultima_foto_Consumo;
    private Uri ruta_foto_Ine_frente;
    private String ultima_foto_ine_frente;
    private Uri ruta_foto_Ine_atras;
    private String ultima_foto_ine_atras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Adding icon aside app name
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.logo);

        database = new Database(this.getApplicationContext());
        gson = new Gson();
        boton_historico_cfe = findViewById(R.id.btn_historico_cfe);
        boton_consumo_de_luz = findViewById(R.id.btn_consumo_de_luz);
        boton_ine_frente = findViewById(R.id.btn_ine_frente);
        boton_ine_atras = findViewById(R.id.btn_ine_atras);
        boton_frente_recibo_cfe = findViewById(R.id.btn_frente_recibo_cfe);
        boton_mapa_area_local = findViewById(R.id.btn_mapa_area_local);
//        checkbox_propietario = findViewById(R.id.chk_propietario);
        texto_mapa_area_local_descripcion = findViewById(R.id.lbl_mapa_area_local_descripcion);

        clientSpinner = findViewById(R.id.spinner_1);
        doorSpinner = findViewById(R.id.spinner_4);
        rdgMasInfo = findViewById(R.id.rad_2);
        rdgPropietario = findViewById(R.id.rad_3); //pendientes los rdg
        rdgCredito = findViewById(R.id.rad_5);
        btn_EnviarPorCorreo = findViewById(R.id.btn_enviar_correo); // pendientes enviar y salir
        btn_LimpiarCampos = findViewById(R.id.btn_borrar_campos);
        btn_Salir = findViewById(R.id.btn_salir);

        cambiarEstadoBoton(boton_historico_cfe, Boolean.TRUE);
        cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.TRUE);
        cambiarEstadoBoton(boton_mapa_area_local, Boolean.TRUE);

        //revisar_historico_cfe_guardado(database, boton_historico_cfe);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cambiarEstadoBoton(boton_historico_cfe, Boolean.TRUE);
            cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.TRUE);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        boton_mapa_area_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapActivity = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(mapActivity);
                Toast.makeText(view.getContext(),R.string.message_loading_map, Toast.LENGTH_SHORT).show();
            }
        });

/*        checkbox_propietario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkbox_propietario.isChecked()){

                    if(boton_historico_cfe.getTag() != null && boton_historico_cfe.getTag().toString().equalsIgnoreCase("View")){
                        boton_historico_cfe.setEnabled(Boolean.TRUE);
                        boton_historico_cfe.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                    }
                    else
                        cambiarEstadoBoton(boton_historico_cfe, Boolean.TRUE);
                    cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.TRUE);
                    cambiarEstadoBoton(boton_mapa_area_local, Boolean.TRUE);
                }
                else{
                    cambiarEstadoBoton(boton_historico_cfe, Boolean.FALSE);
                    cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);
                    cambiarEstadoBoton(boton_mapa_area_local, Boolean.FALSE);
                }
            }
        });*/

        boton_historico_cfe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(boton_historico_cfe.getTag() != null){
                    if( boton_historico_cfe.getTag() != null && boton_historico_cfe.getTag().toString().isEmpty() == false &&  boton_historico_cfe.getTag().toString().equalsIgnoreCase("View")){
                        Intent verImagen = new Intent(getApplicationContext(), ShowImageActivity.class);
                        verImagen.putExtra("imagen",database.getElement(clave_ultimo_archivo));
                        verImagen.putExtra("clave_archivo", clave_ultimo_archivo);
                        verImagen.putExtra("clave_extra", clave_historico);
                        startActivityForResult(verImagen,8);
                    }
                    else
                        tomarFotografiaHistorico();
                }
                else{
                    tomarFotografiaHistorico();
                }

            }
        });
        boton_consumo_de_luz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(boton_consumo_de_luz.getTag() != null){
                    if( boton_consumo_de_luz.getTag() != null &&  boton_consumo_de_luz.getTag().toString().isEmpty() == false &&   boton_consumo_de_luz.getTag().toString().equalsIgnoreCase("View")){
                        Intent verImagen = new Intent(getApplicationContext(), ShowImageActivity.class);
                        verImagen.putExtra("imagen",database.getElement("clave_consumo_luz"));
                        verImagen.putExtra("clave_archivo", "");
                        verImagen.putExtra("clave_extra", "");
                        startActivityForResult(verImagen, 5);
                    }
                    else
                        tomarFotografiaConsumo();
                }
                else{
                    tomarFotografiaConsumo();
                }

            }
        });

        boton_ine_frente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(boton_ine_frente.getTag() != null){
                    if( boton_ine_frente.getTag() != null &&  boton_ine_frente.getTag().toString().isEmpty() == false &&   boton_ine_frente.getTag().toString().equalsIgnoreCase("View")){
                        Intent verImagen = new Intent(getApplicationContext(), ShowImageActivity.class);
                        verImagen.putExtra("imagen",database.getElement("clave_ine_frente"));
                        verImagen.putExtra("clave_archivo", "");
                        verImagen.putExtra("clave_extra", "");
                        startActivityForResult(verImagen, 6);
                    }
                    else
                        tomarFotografiaINEFrente();
                }
                else{
                    tomarFotografiaINEFrente();
                }

            }
        });
        boton_ine_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(boton_ine_atras.getTag() != null){
                    if( boton_ine_atras.getTag() != null &&  boton_ine_atras.getTag().toString().isEmpty() == false &&   boton_ine_atras.getTag().toString().equalsIgnoreCase("View")){
                        Intent verImagen = new Intent(getApplicationContext(), ShowImageActivity.class);
                        verImagen.putExtra("imagen",database.getElement("clave_ine_atras"));
                        verImagen.putExtra("clave_archivo", "");
                        verImagen.putExtra("clave_extra", "");
                        startActivityForResult(verImagen, 7);
                    }
                    else
                        tomarFotografiaINEAtras();
                }
                else{
                    tomarFotografiaINEAtras();
                }

            }
        });

        // Aqui va lo de los spinners
        String[] clientes = {"Seleccione...","EDUARDO PEREZ GOMEZ","EDUARDO QUIJANO VELA","JAIME URRUTIA LOPEZ","JULIO DIAZ MENDOZA","MARTIN CHI PEREZ"};
        clientSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientes));
        puertas = new String[]{"0", "1", "2", "3", "4", "5"};
        doorSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, puertas));

        btn_EnviarPorCorreo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        if(isValidSurvey()){
                            sendSurvey();
                        }
                    }
                }
        );

        btn_LimpiarCampos.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        askConfirmation("Reiniciar Formilario", "¿Desea reiniciar el formulario?");
                    }
                }
        );

        btn_Salir.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        close = true;
                        askConfirmation("Salir de la aplicacion", "¿Desea Salir de la aplicacion?");
                    }
                }
        );
    }

    public void revisar_historico_cfe_guardado(Database database, ImageButton boton){ // Aqui Borra del historico

        String historico = database.getElement(clave_historico);
        String imagen = database.getElement(clave_ultimo_archivo);
        Gson gson = new Gson();
        LinearLayout contenedor_historico = findViewById(R.id.lista_historico);
        contenedor_historico.removeAllViews(); // aqui especificamente
        Type historico_lista_tipo = new TypeToken<ArrayList<Historico>>(){}.getType();
        if(historico != null && imagen != null && historico.isEmpty() == false && imagen.isEmpty() == false){
            File imagen_archivo = new File(imagen);
            ruta_foto_historico = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",imagen_archivo);
            ArrayList<Historico> lista_historico = gson.fromJson(historico, historico_lista_tipo);

            if( lista_historico != null && lista_historico.size() > 0){

                contenedor_historico.setVisibility(View.VISIBLE);
                TextView texto_historico_cfe_descripcion = findViewById(R.id.lbl_historico_cfe_descripcion);
                texto_historico_cfe_descripcion.setText("");

                String historic =  "";

                for(int index = 0; index < lista_historico.size(); index ++){



                    LayoutInflater vista = LayoutInflater.from(getApplicationContext());
                    View elemento = vista.inflate(R.layout.item_historico, null, false);
                    RelativeLayout contenedor_item_historico = elemento.findViewById(R.id.container_item_historico);
                    TextView texto_fecha = elemento.findViewById(R.id.txt_fecha);
                    TextView texto_consumo = elemento.findViewById(R.id.txt_consumo);


                    texto_fecha.setText(lista_historico.get(index).getFecha());
                    texto_consumo.setText(lista_historico.get(index).getConsumo().toString());

                    historic += lista_historico.get(index).getFecha() + " - " + lista_historico.get(index).getConsumo().toString() +"\n";

                    if(index%2 == 0)
                        contenedor_item_historico.setBackground(getDrawable(R.color.colorBackgroundText));
                    contenedor_historico.addView(elemento);

                }
                database.saveHistoric(historic);

                boton.setEnabled(Boolean.TRUE);
                boton.setBackgroundResource(R.color.colorBackground);
                boton.setImageResource(R.mipmap.eye_icon);
                boton.setTag("View");

            }

/*            if(checkbox_propietario.isChecked()) {
                boton.setEnabled(Boolean.TRUE);
                boton.setBackgroundColor(getResources().getColor(R.color.colorBackground));

            }else{
                boton.setEnabled(Boolean.FALSE);
                boton.setBackground(getDrawable(R.drawable.round_button_inactive));

            }*/
        }
        else{

            contenedor_historico.setVisibility(View.GONE);
            TextView texto_historico_cfe_descripcion = findViewById(R.id.lbl_historico_cfe_descripcion);
            texto_historico_cfe_descripcion.setText(R.string.label_frente_recibo_cfe_descripcion);

/*            if(checkbox_propietario.isChecked()) {
                boton.setEnabled(Boolean.TRUE);
                boton.setBackground(getDrawable(R.drawable.round_button_active));
                boton.setImageResource(R.mipmap.angle_right);
                boton.setTag("");
            }else{
                boton.setEnabled(Boolean.FALSE);
                boton.setBackground(getDrawable(R.drawable.round_button_inactive));
                boton.setImageResource(R.mipmap.angle_right);
                boton.setTag("");
            }*/
        }
    }

    public File getCameraFile() {
        //((File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "\CameraDemo");
        return new File(ultima_foto_Historico);
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

    private void tomarFotografiaHistorico(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagenHistorico = getOutputMediaFile();
        ultima_foto_Historico = imagenHistorico.getAbsolutePath();
        ruta_foto_historico = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",imagenHistorico);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ruta_foto_historico);
        ultima_foto_ruta =  ruta_foto_historico;
        startActivityForResult(intent, GUARDAR_FOTO_HISTORICO);
    }

    private void tomarFotografiaConsumo(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagenConsumo = getOutputMediaFile();
        ultima_foto_Consumo = imagenConsumo.getAbsolutePath();
        ruta_foto_Consumo = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",imagenConsumo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ruta_foto_Consumo);
        ultima_foto_ruta = ruta_foto_Consumo;
        startActivityForResult(intent,GUARDAR_FOTO_CONSUMO);
    }

    private void tomarFotografiaINEFrente(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagenINEFrente = getOutputMediaFile();
        ultima_foto_ine_frente = imagenINEFrente.getAbsolutePath();
        ruta_foto_Ine_frente = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",imagenINEFrente);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ruta_foto_Ine_frente);
        ultima_foto_ruta = ruta_foto_Ine_frente;
        startActivityForResult(intent,GUARDAR_INE_FRENTE);
    }

    private void tomarFotografiaINEAtras(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagenINEAtras = getOutputMediaFile();
        ultima_foto_ine_atras = imagenINEAtras.getAbsolutePath();
        ruta_foto_Ine_atras = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",imagenINEAtras);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ruta_foto_Ine_atras);
        ultima_foto_ruta = ruta_foto_Ine_atras;
        startActivityForResult(intent,GUARDAR_INE_ATRAS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case GUARDAR_FOTO_HISTORICO:
                if (resultCode == RESULT_OK) {
                    uploadImage(ultima_foto_ruta);
                }
                break;
            case 2:
                boton_consumo_de_luz.setBackgroundResource(R.color.colorBackground);
                boton_consumo_de_luz.setImageResource(R.mipmap.eye_icon);
                boton_consumo_de_luz.setTag("View");
                database.saveElement("clave_consumo_luz", ultima_foto_Consumo);
                break;
            case 3:
                boton_ine_frente.setBackgroundResource(R.color.colorBackground);
                boton_ine_frente.setImageResource(R.mipmap.eye_icon);
                boton_ine_frente.setTag("View");
                database.saveElement("clave_ine_frente", ultima_foto_ine_frente);
                break;
            case 4:
                boton_ine_atras.setBackgroundResource(R.color.colorBackground);
                boton_ine_atras.setImageResource(R.mipmap.eye_icon);
                boton_ine_atras.setTag("View");
                database.saveElement("clave_ine_atras", ultima_foto_ine_atras);
                break;
            case 5:
                if (resultCode == RESULT_OK) {
                    //deleteImage(ruta_foto_Consumo);
                    ruta_foto_Consumo = null;
                    ultima_foto_Consumo = null;
                    restoreImageButton(boton_consumo_de_luz);
                }
                break;
            case 6:
                if (resultCode == RESULT_OK) {
                    //deleteImage(ruta_foto_Ine_frente);
                    ruta_foto_Ine_frente = null;
                    ultima_foto_ine_frente = null;
                    restoreImageButton(boton_ine_frente);
                }
                break;
            case 7:
                if (resultCode == RESULT_OK) {
                    //deleteImage(ruta_foto_Ine_atras);
                    ruta_foto_Ine_atras = null;
                    ultima_foto_ine_atras = null;
                    restoreImageButton(boton_ine_atras);
                }
                break;
            case 8:
                if (resultCode == RESULT_OK) {
                    //deleteImage(ruta_foto_Ine_atras);
                    restoreImageButton(boton_historico_cfe);
                }
                break;
            default:
        }
    }


    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {

                    // scale the image to save on bandwidth
                    Bitmap bitmap =
                            scaleBitmapDown(
                                    MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                    MAX_DIMENSION);

                    callCloudVision(bitmap);


            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("TEXT_DETECTION");

                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private  class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;
        private String resultado = "";

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LinearLayout contenedor_historico = findViewById(R.id.lista_historico);
            contenedor_historico.removeAllViews();
            Toast.makeText(getApplicationContext(), "Procesando imagen.... Espere unos segundos", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                resultado = response.getResponses().toString();

                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {


            }

            Double promedio = 0.0;
            int contador_interno = 0;

            ArrayList<Historico> historico_filtrado = new ArrayList<>();

            for(int index = 0; index < historico_cfe.size(); index ++){
                if(historico_cfe.get(index).getConsumo() > 0){
                    historico_filtrado.add(historico_cfe.get(index));
                    contador_interno += 1;

                    LinearLayout contenedor_historico = findViewById(R.id.lista_historico);


                    LayoutInflater vista = LayoutInflater.from(getApplicationContext());
                    View elemento = vista.inflate(R.layout.item_historico, null, false);
                    RelativeLayout contenedor_item_historico = elemento.findViewById(R.id.container_item_historico);
                    TextView texto_fecha = elemento.findViewById(R.id.txt_fecha);
                    TextView texto_consumo = elemento.findViewById(R.id.txt_consumo);

                    texto_fecha.setText(historico_cfe.get(index).getFecha());
                    texto_consumo.setText(historico_cfe.get(index).getConsumo().toString());
                    promedio += historico_cfe.get(index).getConsumo();

                    if(contador_interno%2 == 0)
                        contenedor_item_historico.setBackground(getDrawable(R.color.colorBackgroundText));
                    contenedor_historico.addView(elemento);

                }


            }

            if(historico_filtrado.size() > 0){
                boton_historico_cfe.setBackgroundResource(R.color.colorBackground);
                boton_historico_cfe.setImageResource(R.mipmap.eye_icon);
                boton_historico_cfe.setTag("View");
                LinearLayout contenedor_historico = findViewById(R.id.lista_historico);
                contenedor_historico.setVisibility(View.VISIBLE);
                TextView texto_historico_cfe_descripcion = findViewById(R.id.lbl_historico_cfe_descripcion);
                texto_historico_cfe_descripcion.setText("");


                LayoutInflater vista = LayoutInflater.from(getApplicationContext());
                View elemento = vista.inflate(R.layout.item_historico, null, false);
                TextView texto_fecha = elemento.findViewById(R.id.txt_fecha);
                TextView texto_consumo = elemento.findViewById(R.id.txt_consumo);

                texto_fecha.setText("Promedio de consumo");

                promedio = promedio / historico_filtrado.size();
                promedio = new  BigDecimal(promedio.toString()).setScale(2,RoundingMode.HALF_UP).doubleValue();
                texto_consumo.setText(promedio.toString());
                contenedor_historico.addView(elemento);

                Historico historico_promedio = new Historico();
                historico_promedio.setFecha("Promedio de consumo");
                historico_promedio.setConsumo(promedio);
                historico_filtrado.add(historico_promedio);

                String json_historico_cfe = gson.toJson(historico_filtrado);
                database.saveElement(clave_ultimo_archivo, ultima_foto_Historico);
                database.saveElement(clave_historico, json_historico_cfe);
            }
            else{
                Toast.makeText(getApplicationContext(), "No se pudo recuperar el histórico", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading


        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        StringBuilder message = new StringBuilder("I found these things:\n\n");
        Integer contador_maximo = 0;
        historico_cfe = new ArrayList<>();

        try {



        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        String texto = response.getResponses().get(0).getTextAnnotations().get(0).getDescription();
        String [] tokens = texto.split("\n");
        ArrayList<String> historico_fechas = new ArrayList<>();
        ArrayList<Double> historico_consumos= new ArrayList<>();



        for (int index = 0; index < tokens.length; index++ ){
            try{
                Double numero = Double.parseDouble(tokens[index]);
                historico_consumos.add(numero);
            }catch(NumberFormatException | NullPointerException nfe){

                if(tokens[index].toLowerCase().contains("Período".toLowerCase()) || tokens[index].toLowerCase().contains("Energía".toLowerCase()) || tokens[index].toLowerCase().contains("kWh".toLowerCase()) || tokens[index].toLowerCase().contains("Energia".toLowerCase()) || tokens[index].toLowerCase().contains("Periodo".toLowerCase())){

                }
                else
                    historico_fechas.add(tokens[index]);
            }
        }

        if(historico_fechas.size() >= historico_consumos.size()){
            contador_maximo = historico_fechas.size();
        }
        else {
            contador_maximo = historico_consumos.size();
        }

        for (int index = 0; index < contador_maximo; index ++){
            Historico historico = new Historico();
            if(hasIndex(index, historico_fechas))
                historico.setFecha(historico_fechas.get(index));
            if(hasIndex(index, historico_consumos))
                historico.setConsumo(historico_consumos.get(index));

            historico_cfe.add(historico);

        }

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }
        }
        catch (Exception exception){

        }

        return message.toString();
    }

    public boolean hasIndex(int index, ArrayList list){
        if(index < list.size())
            return true;
        return false;
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
        System.out.println("calling resume");
        texto_mapa_area_local_descripcion.setText(new DecimalFormat("##.##").format(database.getCalculatedArea())+" m2");
        revisar_historico_cfe_guardado(database, boton_historico_cfe);

/*        if(checkbox_propietario.isChecked()) {
            cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.TRUE);
            cambiarEstadoBoton(boton_mapa_area_local, Boolean.TRUE);
        }else{
            cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);
            cambiarEstadoBoton(boton_mapa_area_local, Boolean.FALSE);
        }*/

    }

    public void clearSurvey(){

        clientSpinner.setSelection(INDEX_ZERO);
        doorSpinner.setSelection(INDEX_ZERO);

        rdgMasInfo.clearCheck();
        rdgPropietario.clearCheck();
        rdgCredito.clearCheck();

        database.resetAreaMarkers();
        database.resetCalculatedArea();
        texto_mapa_area_local_descripcion.setText(new DecimalFormat("##.##").format(database.getCalculatedArea())+" m2");

        restoreImageButton(boton_historico_cfe);
        LinearLayout contenedor_historico = findViewById(R.id.lista_historico);
        contenedor_historico.removeAllViews(); // aqui especificamente
        database.DeleteElement(clave_historico);
        database.DeleteElement(clave_ultimo_archivo);

        restoreImageButton(boton_consumo_de_luz);
        restoreImageButton(boton_ine_frente);
        restoreImageButton(boton_ine_atras);
    }

    public  void sendSurvey(){
        if(isValidSurvey())
        {
            String name;
            String moreInfo;
            String isOwner;
            String extraDoors;
            String isBureauAuthorized;
            String roofArea;
            String roofCorners;

            RadioButton selectedRadioButton;

            name = clientSpinner.getSelectedItem().toString();
            selectedRadioButton = findViewById(rdgMasInfo.getCheckedRadioButtonId());
            moreInfo = selectedRadioButton.getText().toString();
            selectedRadioButton = findViewById(rdgPropietario.getCheckedRadioButtonId());
            isOwner = selectedRadioButton.getText().toString();
            extraDoors = doorSpinner.getSelectedItemPosition()+"";
            selectedRadioButton = findViewById(rdgCredito.getCheckedRadioButtonId());
            isBureauAuthorized = selectedRadioButton.getText().toString();
            roofArea = database.getCalculatedArea()+"";
            roofCorners = database.getRawAreaMarkers();

            Survey survey;
            survey = new Survey(name, moreInfo, isOwner,extraDoors,isBureauAuthorized,roofArea,roofCorners,ruta_foto_historico,ruta_foto_Consumo,ruta_foto_Ine_frente,ruta_foto_Ine_atras);

            enviarCorreoFormulario(survey);
            Log.d("Resultado", survey.toString());
        }
    }

    private void enviarCorreoFormulario(Survey survey) {

        String[] TO = {"solanummx@gmail.com"}; //Direcciones email  a enviar.
        String[] CC = {}; //Direcciones email con copia.

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        ArrayList<Uri> uris = new ArrayList<Uri>();
        uris.add(ruta_foto_historico);
        uris.add(ruta_foto_Consumo);
        uris.add(ruta_foto_Ine_frente);
        uris.add(ruta_foto_Ine_atras);

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);

        emailIntent.setType("image/*");
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Envío de cuestionario: " + survey.name );
        emailIntent.putExtra(Intent.EXTRA_TEXT, survey.toString() + "\nHistorico: \n"+ database.getHistoric()+"\n");

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar email."));
            Log.i("EMAIL", "Enviando email...");
            //clearSurvey();
        }
        catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No  existe ningún cliente de email instalado!.", Toast.LENGTH_SHORT).show();
        }
    }

    public  void askConfirmation(String title, String message){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (close){
                            clearSurvey();
                            finish();
                        }else{
                            clearSurvey();
                        }

                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public boolean isValidSurvey(){
        String uncheckedFields = "";
        if(clientSpinner.getSelectedItemPosition() == 0){
            uncheckedFields += "1, ";
        }
        if(rdgMasInfo.getCheckedRadioButtonId()==-1)
        {
            uncheckedFields += "2, ";
        }
        if(rdgPropietario.getCheckedRadioButtonId()==-1)
        {
            uncheckedFields += "3, ";
        }
/*        if(doorSpinner.getSelectedItemPosition() == 0){
            uncheckedFields += "4, ";
        }*/
        if (rdgCredito.getCheckedRadioButtonId()==-1){
            uncheckedFields += "5, ";
        }
        if (database.getCalculatedArea() == 0){
            uncheckedFields += "6, ";
        }

        if (clave_historico.equals("") || clave_ultimo_archivo.equals("")){
            uncheckedFields += "7, ";
        }

        if (ruta_foto_Consumo == null || ultima_foto_Consumo.equals("")){
            uncheckedFields += "8, ";
        }

        if (ruta_foto_Ine_frente == null || ultima_foto_ine_frente.equals("")){
            uncheckedFields += "9, ";
        }

        if (ruta_foto_Ine_atras == null || ultima_foto_ine_atras.equals("")){
            uncheckedFields += "10, ";
        }

        if (uncheckedFields.equals("")){
            return true;
        }else{
            Toast.makeText(getApplicationContext(), "No respondio el(los) campo(s) " + uncheckedFields + "revise la encuesta", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private void deleteImage(Uri imageUri){

        File fdelete = new File(getFilePath(imageUri));

        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" );
            } else {
                System.out.println("file not Deleted :");
            }
        }

    }

    private String getFilePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            return picturePath;
        }
        return null;
    }

    private void restoreImageButton(ImageButton boton){
        boton.setBackgroundResource(R.drawable.round_button_active);
        boton.setImageResource(R.mipmap.angle_right);
        boton.setTag("");
    }
}
