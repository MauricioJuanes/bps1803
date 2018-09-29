package com.prototipo.prototipo.prototipo.Maps;

import android.Manifest;
import android.content.pm.PackageManager;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.prototipo.prototipo.prototipo.R;



public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener
{

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;//current location of the user
    private Location mLastLocation; //Last location of the user
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;//current showed map
    private SupportMapFragment mapFragment;
    private Marker currentPositionmarker;

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
            }
        });

        final FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.menu_fab);

        FloatingActionButton showLastLocationButton =  (FloatingActionButton) findViewById(R.id.action_showCurrentLocation);
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
            //Toast.makeText(this, "Stop apps without permission to use location information", Toast.LENGTH_SHORT).show();
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

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    @Override
    public void onMapClick(LatLng latLng) {
        //mMap = googleMap;
        mapFragment.getMapAsync(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }


    private void showCurrentLocationOnMap(){
        initGoogleMapLocation();
    }

}
