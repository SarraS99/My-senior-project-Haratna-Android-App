package com.socialmedia.socialmedia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.socialmedia.socialmedia.databinding.ActivityMapPinnedPostsBinding;

import java.util.List;

public class MapPinnedPostsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapPinnedPostsBinding binding;

    private static final String TAG = "PLACE_PICKER_TAG";
    private static final int DEFAULT_ZOOM = 15;

    private static final long HIGHLIGHT_DISTANCE_KM = 2;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPinnedPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("خريطة المساعدة");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Initialize the Places client
        Places.initialize(this, getString(R.string.google_maps_key));

        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                    Toast.makeText(MapPinnedPostsActivity.this, "الموقع ليس قيد التشغيل! قم بتشغيله لإظهار الموقع الحالي ...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loadPinnedPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String isPinned = "" + ds.child("isPinned").getValue();
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    String pUid = "" + ds.child("uid").getValue();

                    Log.d(TAG, "loadPinnedPosts: onDataChange: isPinned: " + isPinned);
                    if (isPinned.equals("true")) {
                        String postLatitude = "" + ds.child("latitude").getValue();
                        String postLongitude = "" + ds.child("longitude").getValue();

                        Log.d(TAG, "loadPinnedPosts: onDataChange: postLatitude: " + postLatitude);
                        Log.d(TAG, "loadPinnedPosts: onDataChange: postLongitude: " + postLongitude);

                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                        userRef.child(pUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        String title = "" + snapshot.child("name").getValue();
                                        String description = pTitle.replace("null", "") + " " + pDescr.replace("null", "");

                                        LatLng latLng = new LatLng(
                                                Double.parseDouble(postLatitude),
                                                Double.parseDouble(postLongitude)
                                        );
                                        addMarker(latLng, "" + title, "" + description, false);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference BusRef = FirebaseDatabase.getInstance().getReference("BusPosts");
        BusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String isPinned = "" + ds.child("isPinned").getValue();
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    String pUid = "" + ds.child("uid").getValue();
                    Log.d(TAG, "loadPinnedPosts: onDataChange: isPinned: " + isPinned);
                    if (isPinned.equals("true")) {
                        String postLatitude = "" + ds.child("latitude").getValue();
                        String postLongitude = "" + ds.child("longitude").getValue();
                        Log.d(TAG, "loadPinnedPosts: onDataChange: postLatitude: " + postLatitude);
                        Log.d(TAG, "loadPinnedPosts: onDataChange: postLongitude: " + postLongitude);
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                        userRef.child(pUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String title = "" + snapshot.child("name").getValue();
                                        String description = pTitle.replace("null", "") + " " + pDescr.replace("null", "");
                                        LatLng latLng = new LatLng(
                                                Double.parseDouble(postLatitude),
                                                Double.parseDouble(postLongitude)
                                        );
                                        addMarker(latLng, "" + title, "" + description, false);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        //Enable the zoom controls for the map
        //mMap!!.uiSettings.isZoomControlsEnabled = true
        //mMap!!.uiSettings.isCompassEnabled = true

        // Prompt the user for permission.
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "requestLocationPermission: isGranted: " + isGranted);
                    //lets check if from permission dialog user have granted the permission or denied the result is in isGranted as true/false
                    if (isGranted) {
                        //user has granted permission so we can pick image from gallery
                        mMap.setMyLocationEnabled(true);
                        pickCurrentPlace();
                    } else {
                        //user denied permission so we can't pick image from gallery
                        Toast.makeText(MapPinnedPostsActivity.this, "تم رفض الإذن...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void addressFromLatLng(LatLng latLng, Boolean isCurrentLocation) {
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

            addMarker(latLng, "" + subLocality, "" + addressLine, isCurrentLocation);
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

                                //addressFromLatLng(latLng, true);

                                loadPinnedPosts();
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
     * @param latLng      LatLng of the location picked
     * @param title       Title of the location picked
     * @param description Address of the location picked
     */
    private void addMarker(LatLng latLng, String title, String description, Boolean isCurrentLocation) {
        Log.d(TAG, "addMarker: selectedLatitude: " + selectedLatitude);
        Log.d(TAG, "addMarker: selectedLongitude: " + selectedLongitude);
        Log.d(TAG, "addMarker: latitude: " + latLng.latitude);
        Log.d(TAG, "addMarker: longitude: " + latLng.longitude);
        Log.d(TAG, "addMarker: title: " + title);
        Log.d(TAG, "addMarker: description: " + description);
        Log.d(TAG, "addMarker: isCurrentLocation: " + isCurrentLocation);
        //mMap.clear();

        try {
            //Find Distance between user current location and post location
            float[] results = new float[1];
            Location.distanceBetween(selectedLatitude, selectedLongitude, latLng.latitude, latLng.longitude, results);
            //Distance in meters
            double distanceMeter = results[0];
            //Distance in kilo meters
            double distanceKm = distanceMeter / 1000;

            Log.d(TAG, "addMarker: distanceMeter: " + distanceMeter);
            Log.d(TAG, "addMarker: distanceKm: " + distanceKm);

            //Marker Options
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("" + title);
            markerOptions.snippet("" + description);

            if (distanceKm > HIGHLIGHT_DISTANCE_KM) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            mMap.addMarker(markerOptions);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

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