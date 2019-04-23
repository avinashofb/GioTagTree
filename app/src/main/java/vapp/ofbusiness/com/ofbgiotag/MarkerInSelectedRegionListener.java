package vapp.ofbusiness.com.ofbgiotag;

import com.google.android.gms.maps.model.LatLng;

public interface MarkerInSelectedRegionListener {

    void markerInSelectedRegion(LatLng latLng);

    void markerNotInSelectedRegion();

    void getAddressByCoordinates();

}
