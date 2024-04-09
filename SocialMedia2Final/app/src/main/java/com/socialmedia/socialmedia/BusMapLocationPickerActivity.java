package com.socialmedia.socialmedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialmedia.socialmedia.databinding.ActivityBusMapLocationPickerBinding;


import java.util.HashMap;
import java.util.List;

public class BusMapLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityBusMapLocationPickerBinding binding;

    private static final String TAG = "PLACE_PICKER_TAG";
    private static final int DEFAULT_ZOOM = 15;
    private GoogleMap mMap = null;

    // Current Place Picker
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation = null;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";

    private FirebaseAuth firebaseAuth;

    private String postId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBusMapLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("خريطة المساعدة");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        binding.doneLl.setVisibility(View.GONE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Initialize the Places client
        Places.initialize(this, getString(R.string.google_maps_key));

        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        postId = getIntent().getStringExtra("postId");

        firebaseAuth = FirebaseAuth.getInstance();

        binding.toolbarGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // COMMENTED OUT UNTIL WE DEFINE THE METHOD
                // Present the current place picker
                if (isGPSEnabled()) {
                    // Prompt the user for permission.
                    requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    Toast.makeText(BusMapLocationPickerActivity.this, "الموقع ليس قيد التشغيل! قم بتشغيله لإظهار الموقع الحالي ...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //put data to intent to get in previous activity
                /*Intent intent = new Intent();
                intent.putExtra("latitude", selectedLatitude);
                intent.putExtra("longitude", selectedLongitude);
                intent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, intent);
                //finishing activity
                finish();*/
                pinPost();
            }
        });
    }

    private void pinPost() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("انتظر من فضلك");
        progressDialog.show();

        DatabaseReference refSearch = FirebaseDatabase.getInstance().getReference("BusPosts");
        refSearch.orderByChild("uid").equalTo("" + firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String cPostId = "" + ds.child("pId").getValue();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isPinned", false);

                            DatabaseReference refUpdate = FirebaseDatabase.getInstance().getReference("BusPosts");
                            refUpdate.child(cPostId)
                                    .updateChildren(hashMap);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("latitude", selectedLatitude);
                                hashMap.put("longitude", selectedLongitude);
                                hashMap.put("isPinned", true);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusPosts");
                                ref.child(postId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(BusMapLocationPickerActivity.this, "تم التثبيت بنجاح ...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(BusMapLocationPickerActivity.this, "فشل التثبيت بسبب " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }, 2000);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;

        //Enable the zoom controls for the map
        //mMap!!.uiSettings.isZoomControlsEnabled = true
        //mMap!!.uiSettings.isCompassEnabled = true

        // Prompt the user for permission.
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;

                Log.d(TAG, "onMapReady: Selected Latitude: " + selectedLatitude);
                Log.d(TAG, "onMapReady: Selected Longitude: " + selectedLongitude);

                addressFromLatLng(latLng);
            }
        });
    }

    private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "requestLocationPermission: isGranted: " + isGranted);
                    //lets check if from permission dialog user have granted the permission or denied the result is in isGranted as true/false
                    if (isGranted) {
                        //user has granted permission so we can show current location
                        mMap.setMyLocationEnabled(true);
                        pickCurrentPlace();
                    } else {
                        //user denied permission so we can't show current location
                        Toast.makeText(BusMapLocationPickerActivity.this, "تم رفض الإذن...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void addressFromLatLng(LatLng latLng) {
        Log.d(TAG, "addressFromLatLng: ");

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            String countryName = address.getCountryName(); //Country e.g. Pakistan
            String adminArea = address.getAdminArea(); //State e.g. Punjab
            String subAdminArea = address.getSubAdminArea(); //City e.g. Lahore
            String locality = address.getLocality(); //City e.g. Lahore
            String subLocality = address.getSubLocality(); //Neighbourhood e.g. Farooq Colony
            String postalCode = address.getPostalCode(); //Postal Code e.g. 54000

            selectedAddress = "" + addressLine;

            addMarker(latLng, "" + subLocality, "" + addressLine);
        } catch (Exception e) {
            Log.e(TAG, "addressFromLatLng: ", e);
        }

    }

    /**
     * This function will be called only if location permission is granted.
     * We will only check if map object is not null then proceed to show location on map
     */
    private void pickCurrentPlace() {
        Log.d(TAG, "pickCurrentPlace: ");
        if (mMap == null) {
            return;
        }
        detectAndShowDeviceLocationMap();
    }

    /**
     * Get the current location of the device, and position the map's camera
     */
    @SuppressLint("MissingPermission")
    private void detectAndShowDeviceLocationMap() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Set the map's camera position to the current location of the device.
                                mLastKnownLocation = location;

                                selectedLatitude = mLastKnownLocation.getLatitude();
                                selectedLongitude = mLastKnownLocation.getLongitude();

                                Log.d(TAG, "detectAndShowDeviceLocationMap: Selected Latitude: " + selectedLatitude);
                                Log.d(TAG, "detectAndShowDeviceLocationMap: Selected Longitude: " + selectedLongitude);

                                LatLng latLng = new LatLng(selectedLatitude, selectedLongitude);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

                                addressFromLatLng(latLng);
                            } else {
                                Log.d(TAG, "detectAndShowDeviceLocationMap: Current location is null. Using defaults.");
                                //mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat()))
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "detectAndShowDeviceLocationMap: ", e);
        }
    }

    /**
     * Check if GPS/Location is enabled or not
     */
    private boolean isGPSEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "isGPSEnabled: ", e);
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "isGPSEnabled: ", e);
        }
        return !(!gpsEnabled && !networkEnabled);
    }

    /**
     * Add Marker on map after searching/picking location
     *
     * @param latLng  LatLng of the location picked
     * @param title   Title of the location picked
     * @param address Address of the location picked
     */
    private void addMarker(LatLng latLng, String title, String address) {
        Log.d(TAG, "addMarker: latitude: " + latLng.latitude);
        Log.d(TAG, "addMarker: longitude: " + latLng.longitude);
        Log.d(TAG, "addMarker: title: " + title);
        Log.d(TAG, "addMarker: address: " + address);
        mMap.clear();

        try {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("" + title);
            markerOptions.snippet("" + address);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            binding.doneLl.setVisibility(View.VISIBLE);
            binding.selectedPlaceTv.setText(address);
        } catch (Exception e) {
            Log.e(TAG, "addMarker: ", e);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}