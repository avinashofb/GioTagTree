package vapp.ofbusiness.com.ofbgiotag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseLocationActivity extends AppCompatActivity implements  MapWrapperLayout.OnDragListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private ChooseLocationMapFragment mCustomMapFragment;

    public static final String ARG_SELECTED_LAT = "correctLat";
    public static final String ARG_SELECTED_LONG = "correctLong";

    private View mMarkerParentView;
    private ImageView mMarkerImageView;

    private int imageParentWidth = -1;
    private int imageParentHeight = -1;
    private int imageHeight = -1;
    private int centerX = -1;
    private int centerY = -1;

    private double incorrectLat;
    private double incorrectLong;

    private double correctedLat;
    private double correctedLong;

    private TextView mLocationTextView;
    private Button updateLocation;
    private Marker mapMarker;

    private boolean isCurrentLocationInsideCircle = true;

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
                if(isCurrentLocationInsideCircle){
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(ARG_SELECTED_LAT,correctedLat);
                    returnIntent.putExtra(ARG_SELECTED_LONG,correctedLong);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }else{
                    Toast.makeText(ChooseLocationActivity.this, "Please select in" +
                            "side the Circular Region",Toast.LENGTH_LONG).show();
                }
            }
        });

        mCustomMapFragment = (ChooseLocationMapFragment) getFragmentManager().findFragmentById(R.id.map);
        mCustomMapFragment.setOnDragListener(ChooseLocationActivity.this);
        mCustomMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // InitializeUI
        googleMap = map;
        initializeUI();

        if(getIntent().getExtras().containsKey("LAT")){
            incorrectLat = getIntent().getExtras().getDouble("LAT");
            incorrectLong = getIntent().getExtras().getDouble("LONG");
        }

        MapUtils.addMarker(incorrectLat, incorrectLong, "Your Location", mapMarker, googleMap, BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_blue_a200_24dp));
        MapUtils.moveCameraToLocation(incorrectLat, incorrectLong, googleMap);
        MapUtils.showAreaBoundaryCircle(incorrectLat, incorrectLong, googleMap);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
    }


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
        // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
        // 10);
        // googleMap.animateCamera(cameraUpdate);
        // locationManager.removeUpdates(this);
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

            if(MapUtils.getDisplacementBetweenCoordinates(incorrectLat, incorrectLong, centerLatLng.latitude, centerLatLng.longitude) > 100){
                updateLocation.setBackgroundColor(getResources().getColor(R.color.rejectBtColor));
                mLocationTextView.setText("-");
                isCurrentLocationInsideCircle = false;
                return;
            }else{
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

}
