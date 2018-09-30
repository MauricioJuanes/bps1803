package com.prototipo.prototipo.prototipo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.text.DecimalFormat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import com.prototipo.prototipo.prototipo.DataPersistence.Database;

import com.prototipo.prototipo.prototipo.Maps.MapActivity;
import com.prototipo.prototipo.prototipo.Models.Historico;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private  static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private ImageView imageView;
    private ImageButton boton_historico_cfe;
    private ImageButton boton_frente_recibo_cfe;
    private ImageButton boton_mapa_area_local;
    private TextView texto_mapa_area_local_descripcion;
    private CheckBox checkbox_propietario;
    private Uri archivo;
    private Database database;
    private String ultimo_archivo_ubicacion;
    public ArrayList<Historico> historico_cfe;


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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = new Database(this.getApplicationContext());
        boton_historico_cfe = findViewById(R.id.btn_historico_cfe);
        boton_frente_recibo_cfe = findViewById(R.id.btn_frente_recibo_cfe);
        boton_mapa_area_local = findViewById(R.id.btn_mapa_area_local);
        checkbox_propietario = findViewById(R.id.chk_propietario);
        texto_mapa_area_local_descripcion = findViewById(R.id.lbl_mapa_area_local_descripcion);


        cambiarEstadoBoton(boton_historico_cfe, Boolean.FALSE);
        cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);
        cambiarEstadoBoton(boton_mapa_area_local, Boolean.FALSE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cambiarEstadoBoton(boton_historico_cfe, Boolean.FALSE);
            cambiarEstadoBoton(boton_frente_recibo_cfe, Boolean.FALSE);
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

        checkbox_propietario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkbox_propietario.isChecked()){
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
        });

        boton_historico_cfe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFotografia();
            }
        });




    }

    public File getCameraFile() {
        //((File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "\CameraDemo");
        return new File( ultimo_archivo_ubicacion);
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
        File imagen = getOutputMediaFile();
        ultimo_archivo_ubicacion = imagen.getAbsolutePath();
        archivo = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",imagen);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, archivo);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                uploadImage(archivo);
            }
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
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "CameraDemo");

                File file = new File(mediaStorageDir.getPath() + File.separator +
                        "data" + ".txt");
                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(file);
                    stream.write(resultado.getBytes());
                    stream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            Double promedio = 0.0;

            for(int index = 0; index < historico_cfe.size(); index ++){
                LinearLayout contenedor_historico = findViewById(R.id.lista_historico);

                LayoutInflater vista = LayoutInflater.from(getApplicationContext());
                View elemento = vista.inflate(R.layout.item_historico, null, false);
                TextView texto_fecha = elemento.findViewById(R.id.txt_fecha);
                TextView texto_consumo = elemento.findViewById(R.id.txt_consumo);

                texto_fecha.setText(historico_cfe.get(index).getFecha());
                texto_consumo.setText(historico_cfe.get(index).getConsumo().toString());
                promedio += historico_cfe.get(index).getConsumo();
                contenedor_historico.addView(elemento);

            }
            TextView texto_historico_cfe_descripcion = findViewById(R.id.lbl_historico_cfe_descripcion);
            texto_historico_cfe_descripcion.setText("");
            LinearLayout contenedor_historico = findViewById(R.id.lista_historico);

            LayoutInflater vista = LayoutInflater.from(getApplicationContext());
            View elemento = vista.inflate(R.layout.item_historico, null, false);
            TextView texto_fecha = elemento.findViewById(R.id.txt_fecha);
            TextView texto_consumo = elemento.findViewById(R.id.txt_consumo);

            texto_fecha.setText("Promedio de consumo");

            promedio = promedio / historico_cfe.size();
            texto_consumo.setText(promedio.toString());
            contenedor_historico.addView(elemento);

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


        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        String texto = response.getResponses().get(0).getTextAnnotations().get(0).getDescription();
        String [] tokens = texto.split("\n");
        ArrayList<String> historico_fechas = new ArrayList<>();
        ArrayList<Double> historico_consumos= new ArrayList<>();
        historico_cfe = new ArrayList<>();


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
        texto_mapa_area_local_descripcion.setText(new DecimalFormat("##.##").format(database.getCalculatedArea())+" m2");

    }
}
