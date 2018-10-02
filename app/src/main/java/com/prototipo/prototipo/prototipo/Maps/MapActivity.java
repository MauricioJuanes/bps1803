package com.prototipo.prototipo.prototipo.Maps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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
import com.prototipo.prototipo.prototipo.DataPersistence.Database;
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


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener
{

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;//current location of the user
    private Location mLastLocation; //Last location of the user
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;//current showed map
    private SupportMapFragment mapFragment;
    private Marker currentPositionmarker;
    private Boolean isShowingLastPosition = true;

    //Const for area and validations
    public static final int MINIMAL_NUMBER_OF_MARKERS = 3;

    //Data persistence
    private Database database;

    //Calculus of area
    private Context context;
    private ArrayList<LatLng> markerPosition = new ArrayList<>(); //puntos
    private ArrayList<Marker> markerIcons = new ArrayList<>(); //marcadores
    private ArrayList<Polyline> perimeterLines = new ArrayList<>(); //lineas

    //Search locations variables
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private Place searchedLocation;
    private Boolean isSearchng = false;

    //clear map screen
    private Location savedLocationBeforeClear;
    private Boolean isCleaningScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = this;
        this.database = new Database(this.getApplicationContext());

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
                        drawMarker(latLng);
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
                        redrawPerimeter(idMarker);
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

        FloatingActionButton calculateAreaButton = findViewById(R.id.action_calculateArea);
        calculateAreaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateArea();
                floatingActionsMenu.collapse();

            }
        });

        FloatingActionButton showLastPositionButton = findViewById(R.id.action_showLastLocation);
        showLastPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLasttLocationOnMap();
                floatingActionsMenu.collapse();
                Toast.makeText(view.getContext(),R.string.message_showing_last_location, Toast.LENGTH_SHORT).show();

            }
        });

        FloatingActionButton searchLocationButton = findViewById(R.id.action_searchLocation);
        searchLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocation();
                floatingActionsMenu.collapse();

            }
        });

        FloatingActionButton clearScreenMapButton = findViewById(R.id.action_clearMap);
        clearScreenMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clearScreenMap();
                floatingActionsMenu.collapse();

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
                mMap.clear();

                String currentPositionMarkerTitle = getApplicationContext().getString(R.string.current_user_position_title);
                if (isSearchng){
                    mCurrentLocation.setLatitude(searchedLocation.getLatLng().latitude);
                    mCurrentLocation.setLongitude(searchedLocation.getLatLng().longitude);
                    savedLocationBeforeClear = mCurrentLocation;
                    currentPositionMarkerTitle = searchedLocation.getName().toString();
                    isSearchng = false;
                    MarkerOptions options = new MarkerOptions();
                    options.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(currentPositionMarkerTitle);
                    BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    options.icon(icon);
                    currentPositionmarker = mMap.addMarker(options);
                }else{
                    LatLng lastPositionSaved = database.getLastPosition();
                    if(lastPositionSaved.latitude != 0.0 && lastPositionSaved.longitude != 0.0 && isShowingLastPosition){
                        mCurrentLocation.setLatitude(lastPositionSaved.latitude);
                        mCurrentLocation.setLongitude(lastPositionSaved.longitude);
                        savedLocationBeforeClear = mCurrentLocation;
                        List<LatLng> restoredPositions;
                        restoredPositions = database.getAreaMarkers();
                        for (int i=0; i<restoredPositions.size();i++){
                            drawMarker(restoredPositions.get(i));
                        }
                    }else{
                        savedLocationBeforeClear = mCurrentLocation;
                        MarkerOptions options = new MarkerOptions();
                        options.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(currentPositionMarkerTitle);
                        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                        options.icon(icon);
                        currentPositionmarker = mMap.addMarker(options);
                    }
                }

                if (isCleaningScreen){
                    isCleaningScreen = false;
                    mCurrentLocation = savedLocationBeforeClear;
                    mMap.clear();
                    markerPosition = new ArrayList<>();
                    markerIcons = new ArrayList<>();
                    perimeterLines = new ArrayList<>();
                    MarkerOptions options = new MarkerOptions();
                    options.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(currentPositionMarkerTitle);
                    BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    options.icon(icon);
                    currentPositionmarker = mMap.addMarker(options);
                }

                LatLng position = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20));
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
    }

    private void drawMarker(LatLng latLng){

        MarkerOptions options = new MarkerOptions().position(latLng).title("Punto "+markerIcons.size()).draggable(true);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker newMarker = mMap.addMarker(options);
        markerIcons.add(newMarker);
        markerPosition.add(latLng);
        drawPerimeter();

    }

    private void drawPerimeter(){
        int numberOfMarkers = markerPosition.size();

        if (numberOfMarkers > 1){
            LatLng positionA = markerPosition.get(numberOfMarkers-2);
            LatLng positionB = markerPosition.get(numberOfMarkers-1);

            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(positionA, positionB)
                    .width(5)
                    .color(Color.RED));
            perimeterLines.add(line);

            if (numberOfMarkers > 2){
                LatLng firstMarkerPosition = markerPosition.get(0);
                Polyline lineLastToFirst = mMap.addPolyline(new PolylineOptions()
                        .add(positionB, firstMarkerPosition)
                        .width(5)
                        .color(Color.RED));
                if (numberOfMarkers > 3){
                    Polyline lineaUltimoPrimeroAnterior = perimeterLines.get(numberOfMarkers-2);
                    lineaUltimoPrimeroAnterior.remove();
                    Polyline linePenultimateToFinalPosition = perimeterLines.get(numberOfMarkers-1);
                    linePenultimateToFinalPosition.remove();
                    linePenultimateToFinalPosition = mMap.addPolyline(new PolylineOptions()
                            .add(markerPosition.get(numberOfMarkers-2), markerPosition.get(numberOfMarkers-1))
                            .width(5)
                            .color(Color.RED));

                    perimeterLines.set(numberOfMarkers-2, linePenultimateToFinalPosition);
                    perimeterLines.set(numberOfMarkers-1, lineLastToFirst);
                }else {
                    perimeterLines.add(lineLastToFirst);
                }

            }
        }
    }

    private void redrawPerimeter(int idMarker){
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

    private void calculateArea(){
        int numberMarkersOnMap = this.markerIcons.size();
        if (numberMarkersOnMap < MINIMAL_NUMBER_OF_MARKERS){
            Toast.makeText(this.context,R.string.message_error_less_markers_on_map, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this.context,R.string.message_calculating_area, Toast.LENGTH_SHORT).show();
            String savedPosition = markerPosition.get(0).latitude+","+markerPosition.get(0).longitude;
            database.saveCalculatedArea(computeArea(this.markerPosition));
            database.saveCurrentPosition(savedPosition);
            database.saveAreaMarkers(markerPosition);
            finish();
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
        isShowingLastPosition = false;
        initGoogleMapLocation();
        markerPosition = new ArrayList<>();
        markerIcons = new ArrayList<>();
        perimeterLines = new ArrayList<>();
    }

    private void showLasttLocationOnMap(){
        isShowingLastPosition = true;
        markerPosition = new ArrayList<>();
        markerIcons = new ArrayList<>();
        perimeterLines = new ArrayList<>();
        initGoogleMapLocation();



    }

    private void searchLocation(){
        isSearchng = true;
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                searchedLocation = PlaceAutocomplete.getPlace(this, data);
                markerPosition = new ArrayList<>();
                markerIcons = new ArrayList<>();
                perimeterLines = new ArrayList<>();
                initGoogleMapLocation();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(getApplicationContext(), R.string.message_error_searching, Toast.LENGTH_SHORT ).show();
                isSearchng = false;
            } else if (resultCode == RESULT_CANCELED) {
                isSearchng = false;
            }
        }
    }

    public void clearScreenMap(){
        isCleaningScreen = true;
        initGoogleMapLocation();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

}
