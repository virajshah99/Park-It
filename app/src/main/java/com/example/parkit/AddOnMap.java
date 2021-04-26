package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddOnMap extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        TimePickerFragment.TimePickerListener {

    private GoogleMap mMap;
    private PlacesClient placesClient;

    private boolean mLocationPermissionGranted = false;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_code = 101;
    FirebaseFirestore fStore;
    String yourLoc,address;
    AutoCompleteTextView sourceEditText;
    LatLng sLatLng;
    FirebaseDatabase firebaseDatabase;
    Double latitude,longitude;
    public void setCancel(View view){
        TextView setSTime = (TextView) findViewById(R.id.setSTime);
        TextView setETime = (TextView) findViewById(R.id.setETime);
        EditText setAddress = (EditText) findViewById(R.id.sAddress);
        EditText setCost = (EditText) findViewById(R.id.setCost);

        setAddress.setText("");
        setETime.setText("");
        setCost.setText("");
        setSTime.setText("");

        LinearLayout linearLayout = findViewById(R.id.setRentDetail);
        linearLayout.setVisibility(View.GONE);
        Button button = findViewById(R.id.locBtn);
        button.setVisibility(View.VISIBLE);
    }

    public void setDone(View view){
        TextView setSTime = (TextView) findViewById(R.id.setSTime);
        TextView setETime = (TextView) findViewById(R.id.setETime);
        EditText setAddress = (EditText) findViewById(R.id.sAddress);
        EditText setCost = (EditText) findViewById(R.id.setCost);

        String sTime="",eTime="",addr="";
        int cst = -1;
        sTime = String.valueOf(setSTime.getText());
        eTime = String.valueOf(setETime.getText());
        addr = String.valueOf(setAddress.getText());
        cst = Integer.parseInt(String.valueOf(setCost.getText()));
        if(!sTime.equals("") && !eTime.equals("") && !addr.equals("") && cst != -1){
            DocumentReference documentReference = fStore.collection("Parking Spots").document(addr);
            Map<String,Object> user = new HashMap<>();
            //user.put("Geolocation",slatlng);
            user.put("Name",addr);
            user.put("Latitude",latitude);
            user.put("Longitude",longitude);
            user.put("Rate",cst);
            user.put("Availabe Slots",1);
            user.put("Revenue",0);
            user.put("Total Slots",1);
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(AddOnMap.this, "Parking Location Successfully Stored", Toast.LENGTH_SHORT).show();
                }
            });

            DatabaseReference ref = firebaseDatabase.getInstance().getReference("Parking Spots");
            //REALTIME DATABASE CODE
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(addr,new GeoLocation(sLatLng.latitude,sLatLng.longitude),new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String Name, DatabaseError error) {
                    if (error != null) {
                        Toast.makeText(AddOnMap.this, "Parking location Successfully stored", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(AddOnMap.this, "You can store !" + Name, Toast.LENGTH_SHORT).show();
                }
            });





            SharedPreferences sharedPreferences = AddOnMap.this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("rentSTime", sTime).apply();
            sharedPreferences.edit().putString("rentETime", eTime).apply();
            sharedPreferences.edit().putString("rentAddress", addr).apply();
            sharedPreferences.edit().putInt("rentCost", cst).apply();

            Intent intent = new Intent(getApplicationContext(),RentSpot.class);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(AddOnMap.this,"Please give valid inputs", Toast.LENGTH_SHORT).show();
        }
    }

    boolean sst = false, set = false;

    public void getTime1(View view){
        sst = true;
        set = false;
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setCancelable(false);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void getTime2(View view){
        set = true;
        sst = false;
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setCancelable(false);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    //required for picking time
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String min =  String.valueOf(minute);
        String hrs =  String.valueOf(hour);
        if(hrs.length() == 1){
            hrs = "0" + hrs;
        }
        if(min.length() == 1){
            min = "0"+ min;
        }
        TextView setSTime = (TextView) findViewById(R.id.setSTime);
        TextView setETime = (TextView) findViewById(R.id.setETime);

        Log.i("Time",hrs + " : " + min );
        try {
            String s = hrs + " : " + min;
            if (sst && !set) {
                setSTime.setText(s);
            }
            if (set && !sst) {
                setETime.setText(s);
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastLocation();
                mLocationPermissionGranted = true;
                /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }*/
            }
        }
    }

    //to get user last location
    public void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_code);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(AddOnMap.this);
                }
            }
        });
    }

    public void bckBtnPressed(View view) {
        Intent intent = new Intent(getApplicationContext(),RentSpot.class);
        startActivity(intent);
        finish();
    }

    //requesting permissions
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    //for checking net service
    private boolean isNetConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Connect to wifi or quit")
                    .setCancelable(false)
                    .setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();
                            System.exit(0);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
        return true;
    }

    //request for enabling gps
    public boolean isMapsEnabled() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGpsIntent, 9003);//PERMISSIONS_REQUEST_ENABLE_GPS
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        return true;
    }

    //getting permissions
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 9002);//PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        }
    }

    //google play service
    public boolean isServicesOK() {
        Log.d("TAG", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AddOnMap.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AddOnMap.this, available, 9001);//ERROR_DIALOG_REQUEST
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //button  click function
    public void userLocation(View view) {
        try {
            LatLng curLoc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 15));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_on_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fStore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        //enabling location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        //checking services
        checkMapServices();
        isNetConnected();


        Places.initialize(getApplicationContext(), "AIzaSyCbNQKZqT1myNkOfJiDCBe2jGEQamf52dQ");
        placesClient = Places.createClient(this);


        sourceEditText = (AutoCompleteTextView) findViewById(R.id.searchAutoCompleteTextView);
        sourceEditText.setFocusable(false);
        sourceEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldlist = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                String temp = sourceEditText.getText().toString();
                if (temp.isEmpty()) {
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").setInitialQuery(yourLoc).build(AddOnMap.this);
                    startActivityForResult(intent, 200);
                } else {
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").setInitialQuery(temp).build(AddOnMap.this);
                    startActivityForResult(intent, 200);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: called.");
        if (requestCode == 9003) {
            if (!mLocationPermissionGranted) {
                getLocationPermission();
            }
        }
        //source marker
        if (requestCode == 200 & resultCode == RESULT_OK) {

            Place place = Autocomplete.getPlaceFromIntent(data);

            sLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            //for adding marker

            address = place.getName();
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(sLatLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sLatLng, 15));

            sourceEditText.setText(place.getName() + ", " + place.getAddress());

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setBuildingsEnabled(true);

//        mMap.setOnMarkerClickListener(this);

        //customizing maps
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));
            if (!success) {
                Log.e("Map Activity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Map Activity", "Can't find style. Error: ", e);
        }

        //fetch user location at the start
        if (currentLocation != null) {
            //for defining user location in source box
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                if (listAddresses != null && listAddresses.size() > 0) {
                    String addr = "";
                    if (listAddresses.get(0).getAddressLine(0) != null) {
                        addr = listAddresses.get(0).getAddressLine(0);
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        String[] temp = addr.split(", ");
                        Log.i("address", Arrays.toString(temp));

                        int i = 0;
                        while (temp[i] != null) {
                            Boolean numeric = false;
                            if (temp[i].matches("[0-9]+")) {
                                numeric = true;
                            }
                            if (!numeric) {
                                yourLoc = temp[i];
                                break;
                            }
                            i++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("Error", e.toString());
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Log.i("marker",marker.getTitle());
        String s = marker.getTitle();
        EditText setAddress = (EditText) findViewById(R.id.sAddress);
        setAddress.setText(s);
        sLatLng = marker.getPosition();
        Log.i("Slatlng",sLatLng.toString());
        latitude = sLatLng.latitude;
        longitude = sLatLng.longitude;

        LinearLayout linearLayout = findViewById(R.id.setRentDetail);
        linearLayout.setVisibility(View.VISIBLE);
        Button button = findViewById(R.id.locBtn);
        button.setVisibility(View.GONE);

    }
}