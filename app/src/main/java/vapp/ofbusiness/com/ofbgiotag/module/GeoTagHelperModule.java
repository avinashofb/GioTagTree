package vapp.ofbusiness.com.ofbgiotag.module;

import android.app.Activity;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vapp.ofbusiness.com.ofbgiotag.listener.LocationPickerListener;
import vapp.ofbusiness.com.ofbgiotag.utils.MapUtils;

public class GeoTagHelperModule {

    private LocationPickerListener locationPickerListener;
    public GoogleMap googleMap;
    private Activity activity;
    public Location lastKnownLocation;

    private double circleRadius;

    private int centerX = -1;
    private int centerY = -1;
    private int imageParentWidth = -1;
    private int imageParentHeight = -1;
    private int imageHeight = -1;

    public GeoTagHelperModule() {

    }

    public GeoTagHelperModule(LocationPickerListener locationPickerListener, GoogleMap googleMap,
                              Activity activity, double circleRadius, Location lastKnownLocation) {
        this.locationPickerListener = locationPickerListener;
        this.googleMap = googleMap;
        this.activity = activity;
        this.circleRadius = circleRadius;
        this.lastKnownLocation = lastKnownLocation;
    }

//    public void initMapFragment(ChooseLocationMapFragment chooseLocationMapFragment){
////        chooseLocationMapFragment.setOnDragListener(GeoTagHelperModule.this);
////    }

    public void updateLocation(LatLng centerLatLng) {
        if (centerLatLng != null) {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

            if (MapUtils.getDisplacementBetweenCoordinates(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), centerLatLng.latitude, centerLatLng.longitude) > circleRadius) {
                locationPickerListener.markerNotInSelectedRegion();
                return;
            } else {
                locationPickerListener.markerInSelectedRegion(centerLatLng);
            }

            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocation(centerLatLng.latitude, centerLatLng.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                String addressIndex0 = (addresses.get(0).getAddressLine(0) != null) ? addresses
                        .get(0).getAddressLine(0) : "";
                String addressIndex1 = (addresses.get(0).getAddressLine(1) != null) ? addresses
                        .get(0).getAddressLine(1) : "";
                String addressIndex2 = (addresses.get(0).getAddressLine(2) != null) ? addresses
                        .get(0).getAddressLine(2) : "";
                String addressIndex3 = (addresses.get(0).getAddressLine(3) != null) ? addresses
                        .get(0).getAddressLine(3) : "";

                String completeAddress = addressIndex0 + "," + addressIndex1;

                if (addressIndex2 != null) {
                    completeAddress += "," + addressIndex2;
                }
                if (addressIndex3 != null) {
                    completeAddress += "," + addressIndex3;
                }
                if (completeAddress != null) {
                    locationPickerListener.getAddressByCoordinates();
                }
            }
        }
    }

    public void onWindowFocusChanged(Activity activity, View markerParentView, ImageView markerImageView) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        imageParentWidth = markerParentView.getWidth();
        imageParentHeight = markerParentView.getHeight();
        imageHeight = markerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);
    }

    public void dragListner(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Projection projection = (googleMap != null && googleMap.getProjection() != null) ? googleMap.getProjection() : null;
            if (projection != null) {
                LatLng centerLatLng = projection.fromScreenLocation(new Point(centerX, centerY));
                updateLocation(centerLatLng);
            }
        }
    }
}
