package vapp.ofbusiness.com.ofbgiotag;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseLocationActivity extends AppCompatActivity implements MapWrapperLayout.OnDragListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private ChooseLocationMapFragment chooseLocationMapFragment;
    private Location lastKnownLocation;

    public static final String ARG_SELECTED_LAT = "correctLat";
    public static final String ARG_SELECTED_LONG = "correctLong";

    private View mMarkerParentView;
    private ImageView mMarkerImageView;

    private int imageParentWidth = -1;
    private int imageParentHeight = -1;
    private int imageHeight = -1;
    private int centerX = -1;
    private int centerY = -1;

    private double correctedLat;
    private double correctedLong;

    private TextView mLocationTextView;
    private Button updateLocation;
    private Marker mapMarker;

    private boolean isCurrentLocationInsideCircle = true;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_locatio_new);

        mLocationTextView = findViewById(R.id.location_text_view);
        mMarkerParentView = findViewById(R.id.marker_view_incl);
        mMarkerImageView = findViewById(R.id.marker_icon_view);
        updateLocation = findViewById(R.id.update_location_bt);

        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCurrentLocationInsideCircle) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(ARG_SELECTED_LAT, correctedLat);
                    returnIntent.putExtra(ARG_SELECTED_LONG, correctedLong);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(ChooseLocationActivity.this, "Please select in" +
                            "side the Circular Region", Toast.LENGTH_LONG).show();
                }
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        chooseLocationMapFragment = (ChooseLocationMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        chooseLocationMapFragment.setOnDragListener(ChooseLocationActivity.this);
        chooseLocationMapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        checkStoragePermission();

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        initializeUI();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                lastKnownLocation = location;
                //MapUtils.moveCameraToLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap);
                MapUtils.addMarker(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), "Your Location", mapMarker, googleMap, BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_blue_a200_24dp));
                MapUtils.moveCameraToLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap);
                MapUtils.showAreaBoundaryCircle(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap);
            }

        }
    };

    private void initializeUI() {
        try {
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initilizeMap() {
        if (googleMap == null) {
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        imageParentWidth = mMarkerParentView.getWidth();
        imageParentHeight = mMarkerParentView.getHeight();
        imageHeight = mMarkerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);
    }


    @Override
    public void onDrag(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Projection projection = (googleMap != null && googleMap
                    .getProjection() != null) ? googleMap.getProjection()
                    : null;
            //
            if (projection != null) {
                LatLng centerLatLng = projection.fromScreenLocation(new Point(centerX, centerY));
                updateLocation(centerLatLng);
            }
        }
    }

    private void updateLocation(LatLng centerLatLng) {
        if (centerLatLng != null) {
            Geocoder geocoder = new Geocoder(ChooseLocationActivity.this, Locale.getDefault());

            if (MapUtils.getDisplacementBetweenCoordinates(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), centerLatLng.latitude, centerLatLng.longitude) > 100) {
                updateLocation.setBackgroundColor(getResources().getColor(R.color.rejectBtColor));
                mLocationTextView.setText("-");
                isCurrentLocationInsideCircle = false;
                return;
            } else {
                correctedLat = centerLatLng.latitude;
                correctedLong = centerLatLng.longitude;
                updateLocation.setBackgroundColor(getResources().getColor(R.color.acceptBtColor));
                isCurrentLocationInsideCircle = true;
            }

            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocation(centerLatLng.latitude, centerLatLng.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                String addressIndex0 = (addresses.get(0).getAddressLine(0) != null) ? addresses
                        .get(0).getAddressLine(0) : null;
                String addressIndex1 = (addresses.get(0).getAddressLine(1) != null) ? addresses
                        .get(0).getAddressLine(1) : null;
                String addressIndex2 = (addresses.get(0).getAddressLine(2) != null) ? addresses
                        .get(0).getAddressLine(2) : null;
                String addressIndex3 = (addresses.get(0).getAddressLine(3) != null) ? addresses
                        .get(0).getAddressLine(3) : null;

                String completeAddress = addressIndex0 + "," + addressIndex1;

                if (addressIndex2 != null) {
                    completeAddress += "," + addressIndex2;
                }
                if (addressIndex3 != null) {
                    completeAddress += "," + addressIndex3;
                }
                if (completeAddress != null) {
                    mLocationTextView.setText(completeAddress);
                }
            }
        }
    }

    private void checkStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            allPermissionAreMandatory();
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void allPermissionAreMandatory(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.storage_permission_title));
        builder.setMessage(getResources().getString(R.string.storage_permission_desc));
        builder.setPositiveButton(getResources().getString(R.string.lets_do),
                (dialogInterface, i) -> {
                    // permission is denied permenantly, navigate user to app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
        builder.setNegativeButton(getResources().getString(R.string.later),
                (dialogInterface, i) -> {
                });

        builder.setCancelable(true);
        builder.show();
    }

}
