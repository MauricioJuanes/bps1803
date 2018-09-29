package com.prototipo.prototipo.prototipo.Maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.prototipo.prototipo.prototipo.R;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener
{

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;//current location of the user
    private Location mLastLocation; //Last location of the user
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;//current showed map
    private SupportMapFragment mapFragment;
    private Marker currentPositionmarker;

    //Calculus of area
    private ArrayList<LatLng> markerPosition = new ArrayList<>(); //puntos
    private ArrayList<Marker> markerIcons = new ArrayList<>(); //marcadores
    private ArrayList<Polyline> perimeterLines = new ArrayList<>(); //lineas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkMyPermissionLocation();
        } else {
            initGoogleMapLocation();
        }


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.getUiSettings().setZoomControlsEnabled(false);


                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        dibujarMarcador(latLng);
                        System.out.println("hello");

                    }
                });

                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        //Do nothing
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                        //Do nothing

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        //Log.d("Area", "El Area Es: "+ computeArea(puntos));
                        int idMarker = markerIcons.indexOf(marker);
                        reajustarLineas(idMarker);
                    }
                });

            }
        });

        final FloatingActionsMenu floatingActionsMenu = findViewById(R.id.menu_fab);

        FloatingActionButton showLastLocationButton = findViewById(R.id.action_showCurrentLocation);
        showLastLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                showCurrentLocationOnMap();
                floatingActionsMenu.collapse();
                Toast.makeText(view.getContext(),R.string.message_showing_current_location, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkMyPermissionLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permission Check
            PermissionUtils.requestPermission(this);
        } else {
            //If you're authorized, start setting your location
            initGoogleMapLocation();
        }
    }

    private void initGoogleMapLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
                //mCurrentLocation = locationResult.getLastLocation();
                mCurrentLocation = result.getLocations().get(0);
                if(mCurrentLocation!=null)
                {
                    Log.e("Location(Lat)==",""+mCurrentLocation.getLatitude());
                    Log.e("Location(Long)==",""+mCurrentLocation.getLongitude());
                }
                mMap.clear();
                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(getApplicationContext().getString(R.string.current_user_position_title));
                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                options.icon(icon);
                currentPositionmarker = mMap.addMarker(options);
                LatLng position = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }

            @Override
            public void onLocationAvailability(LocationAvailability availability) {
                //boolean isLocation = availability.isLocationAvailable();
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        //To get location information only once here
        mLocationRequest.setNumUpdates(3);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();
        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        });
        locationResponse.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        //If the request code does not match
        if (requestCode != PermissionUtils.REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, grantResults)) {
            //If you have permission, go to the code to get the location value
            initGoogleMapLocation();
        } else {
            Toast.makeText(this, "Stop apps without permission to use location information", Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

   @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        else {

            mMap.setMyLocationEnabled(true);
        }






    }

    public void dibujarMarcador(LatLng latLng){
        double lat = latLng.latitude;
        double lon = latLng.longitude;

        Marker nuevoMarcador = mMap.addMarker(new MarkerOptions().position(latLng).title(lat+"\n"+lon).draggable(true));
        markerIcons.add(nuevoMarcador);
        markerPosition.add(latLng);
        int tamano = markerPosition.size();

        if (tamano > 1){
            LatLng puntoA = markerPosition.get(tamano-2);
            LatLng puntoB = markerPosition.get(tamano-1);

            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(puntoA, puntoB)
                    .width(5)
                    .color(Color.RED));
            perimeterLines.add(line);

            if (tamano > 2){
                LatLng primerPunto = markerPosition.get(0);
                Polyline lineaUltimoPrimero = mMap.addPolyline(new PolylineOptions()
                        .add(puntoB, primerPunto)
                        .width(5)
                        .color(Color.RED));
                if (tamano > 3){
                    Polyline lineaUltimoPrimeroAnterior = perimeterLines.get(tamano-2);
                    lineaUltimoPrimeroAnterior.remove();
                    Polyline lineaPenultimoUltimo = perimeterLines.get(tamano-3);

                    perimeterLines.set(tamano-2, lineaPenultimoUltimo);
                    perimeterLines.set(tamano-1, lineaUltimoPrimero);
                }else {
                    perimeterLines.add(lineaUltimoPrimero);
                }
            }
        }
    }

    public void reajustarLineas(int idMarker){
        int size = markerIcons.size();
        if (size > 0){
            int idB = idMarker;
            markerPosition.set(idB,markerIcons.get(idB).getPosition());
            LatLng positionB = markerPosition.get(idB);

            if (size == 2){
                int idA = (idMarker-1 + size)%size;
                markerPosition.set(idA,markerIcons.get(idA).getPosition());
                LatLng positionA = markerPosition.get(idA);

                if (idMarker == 0){
                    Polyline lineaPosterior = perimeterLines.get(idB);
                    lineaPosterior.remove();

                    Polyline nuevaLineaPosterior = mMap.addPolyline(new PolylineOptions()
                            .add(positionA, positionB)
                            .width(5)
                            .color(Color.RED));
                    perimeterLines.set(idB,nuevaLineaPosterior);

                }

                if (idMarker == 1){
                    Polyline lineaPrevia = perimeterLines.get(idA);
                    lineaPrevia.remove();

                    Polyline nuevaLineaPrevia = mMap.addPolyline(new PolylineOptions()
                            .add(positionA, positionB)
                            .width(5)
                            .color(Color.RED));
                    perimeterLines.set(idA,nuevaLineaPrevia);
                }
            }


            if (size > 2){
                int idA = (idMarker-1 + size)%size;

                Polyline lineaPrevia = perimeterLines.get(idA);
                Polyline lineaPosterior = perimeterLines.get(idB);
                lineaPrevia.remove();
                lineaPosterior.remove();

                markerPosition.set(idA,markerIcons.get(idA).getPosition());
                LatLng puntoA = markerPosition.get(idA);

                Polyline nuevaLineaPrevia = mMap.addPolyline(new PolylineOptions()
                        .add(puntoA, positionB)
                        .width(5)
                        .color(Color.RED));
                perimeterLines.set(idA,nuevaLineaPrevia);

                int idC = (idMarker+1)%size;
                markerPosition.set(idC,markerIcons.get(idC).getPosition());
                LatLng puntoC = markerPosition.get(idC);

                Polyline nuevaLineaPosterior = mMap.addPolyline(new PolylineOptions()
                        .add(positionB, puntoC)
                        .width(5)
                        .color(Color.RED));
                perimeterLines.set(idB,nuevaLineaPosterior);
            }

        }

    }

    /**
     * Returns the area of a closed path on Earth.
     * @param path A closed path.
     * @return The path's area in square meters.
     */
    public static double computeArea(List<LatLng> path) {
        return abs(computeSignedArea(path));
    }

    /**
     * Returns the signed area of a closed path on Earth. The sign of the area may be used to
     * determine the orientation of the path.
     * "inside" is the surface that does not contain the South Pole.
     * @param path A closed path.
     * @return The loop's area in square meters.
     */
    public static double computeSignedArea(List<LatLng> path) {
        double EARTH_RADIUS = 6371009;
        return computeSignedArea(path, EARTH_RADIUS);
    }

    /**
     * Returns the signed area of a closed path on a sphere of given radius.
     * The computed area uses the same units as the radius squared.
     * Used by SphericalUtilTest.
     */
    static double computeSignedArea(List<LatLng> path, double radius) {
        int size = path.size();
        if (size < 3) { return 0; }
        double total = 0;
        LatLng prev = path.get(size - 1);
        double prevTanLat = tan((PI / 2 - toRadians(prev.latitude)) / 2);
        double prevLng = toRadians(prev.longitude);
        // For each edge, accumulate the signed area of the triangle formed by the North Pole
        // and that edge ("polar triangle").
        for (LatLng point : path) {
            double tanLat = tan((PI / 2 - toRadians(point.latitude)) / 2);
            double lng = toRadians(point.longitude);
            total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
            prevTanLat = tanLat;
            prevLng = lng;
        }
        return total * (radius * radius);
    }

    /**
     * Returns the signed area of a triangle which has North Pole as a vertex.
     * Formula derived from "Area of a spherical triangle given two edges and the included angle"
     * as per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
     * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71
     * The arguments named "tan" are tan((pi/2 - latitude)/2).
     */
    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2 * atan2(t * sin(deltaLng), 1 + t * cos(deltaLng));
    }


    private void showCurrentLocationOnMap(){
        initGoogleMapLocation();
    }


    @Override
    public void onMapLongClick(LatLng latLng) {

    }
}
