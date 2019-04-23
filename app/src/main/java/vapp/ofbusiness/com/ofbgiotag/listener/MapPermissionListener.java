package vapp.ofbusiness.com.ofbgiotag.listener;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;

import java.util.List;

public interface MapPermissionListener {

    void allPermissionGranted(MultiplePermissionsReport report);

    void allPermissionNotGranted(MultiplePermissionsReport report);

    void onPermissionRationaleShouldBeShownNote(List<PermissionRequest> permissions, PermissionToken token);
}
