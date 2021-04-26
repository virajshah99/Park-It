package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.net.Uri;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Location currentlocation;
    EditText editText, editText1;
    View mapView;
    TextView textView1, textView2, name;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore fStore;
    LatLng slatlng = null, slatlng1 = null;
    Button mylocation;
    Button fetchdata1;
    double radius = 1;
    String Name, uname;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    LocationListener locationListener;
    LocationManager locationManager;
    String yourlocation, username;
    Button signout, qrcode;
    //name = findViewById(R.id.userName);


    private boolean mLocationPermissionGranted = false;
    //requesting permissions

    boolean freeParkingEnabled = false;

    //Variables for drawer Layout
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    //Toolbar toolbar;
    Button navButton;


    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 9002);//PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        }
    }

    public boolean isServicesOK() {
        Log.d("TAG", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, 9001);//ERROR_DIALOG_REQUEST
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void navButtonPressed(View view) {
        //hooks for drawer layout
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        navButton = (Button) findViewById(R.id.navButton);
        drawerLayout.openDrawer(GravityCompat.START);

        navigationView.bringToFront();
        setupDrawerContent(navigationView);
    }

    //activity when item is selected in nav drawer
    public void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.i("Clicked", "Clicked");
                item.setChecked(true);
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        break;
                    case R.id.nav_pass:
                        //Toast.makeText(MapsActivity.this, "NAv pass", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(getApplicationContext(), changepass.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_mail:
                        //Toast.makeText(MapsActivity.this, "NAv email", f.LENGTH_SHORT).show();
                        Intent intent2 = new Intent(getApplicationContext(), changeemail.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_signOut:
                        //Toast.makeText(MapsActivity.this, "You have Logged Out", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        Toast.makeText(MapsActivity.this, firebaseUser.getEmail() + " " + "You have signed out", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MapsActivity.this, PhoneLoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        break;

                    case R.id.nav_rent:
                        Intent inten = new Intent(getApplicationContext(), RentSpot.class);
                        startActivity(inten);
                        break;

                    case R.id.nav_deleteacc: {
                        new androidx.appcompat.app.AlertDialog.Builder(MapsActivity.this)
                                .setIcon(android.R.drawable.alert_dark_frame)
                                .setTitle("Alert")
                                .setMessage("Your account will be deleted permanently\nAre you sure you want to delete Your account?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Toast.makeText(MapsActivity.this,"Its done",Toast.LENGTH_LONG).show();

                                        firebaseUser.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.i("DELETED", "User account deleted.");
                                                            Toast.makeText(getApplicationContext(), "Your account deleted", Toast.LENGTH_SHORT).show();
                                                            Intent i = new Intent(MapsActivity.this, PhoneLoginActivity.class);
                                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                        }
                                                    }
                                                });

                                        //Enter code here
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        break;
                    }

                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    //for nav drawer back button functionality
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //sets filter to search free parking or both
    public void filterParkingResult(View view) {
        Button button = (Button) findViewById(R.id.freeParkOnly);
        String c = button.getText().toString();
        if (c == "PF") {
            button.setText("F");
            freeParkingEnabled = true;
            if (addresses.size() == 2) {
                if (!addresses.get(0).isEmpty()) {
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(slatlng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));


                }
            } else {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));
            }
            latLngList.clear();
            keylist.clear();
            fetchParkingSpot();
        } else {
            button.setText("PF");
            freeParkingEnabled = false;
            if (addresses.size() == 2) {
                if (!addresses.get(0).isEmpty()) {
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(slatlng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));


                }
            } else {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));
            }
            latLngList.clear();
            keylist.clear();
            fetchParkingSpot();
        }
    }

    //TODO: F& P on marker, booking details, balance, - on booking confirmation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkMapServices();
        name = findViewById(R.id.userName);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mylocation = findViewById(R.id.button2);
        fStore = FirebaseFirestore.getInstance();
        Context context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fetchLastLocation();
        editText = findViewById(R.id.edit_text1);
        textView1 = findViewById(R.id.textView1);
        editText1 = findViewById(R.id.edit_text2);
        LatLng slatlng;
        //fetchdata1 = findViewById(R.id.button3);
        mylocation = findViewById(R.id.button2);

        //for setting name in navigation bar
        username = firebaseUser.getDisplayName();
        Log.i("name", username);
        uname = "Hello !\n" + username;
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView name = (TextView) headerView.findViewById(R.id.userName);
        name.setText(uname);
        this.setupDrawerContent(navigationView);


        Places.initialize(getApplicationContext(), "AIzaSyCbNQKZqT1myNkOfJiDCBe2jGEQamf52dQ");

        editText.setFocusable(false);
        editText1.setFocusable(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("path", String.valueOf(FirebaseFirestore.getInstance().collection("Parking Spots").document("Moksh Plaza")));
                List<Place.Field> fieldlist = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                String temp = editText.getText().toString();
                if (temp.isEmpty()) {
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").setInitialQuery(yourlocation).build(MapsActivity.this);
                    startActivityForResult(intent, 100);
                } else {
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").build(MapsActivity.this);
                    startActivityForResult(intent, 100);
                }
            }
        });

        editText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldlist = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldlist).setCountry("IN").build(MapsActivity.this);

                startActivityForResult(intent, 101);
            }
        });


    }

    //===================================================================================SOURCE AND DESTINATION CODE ==================================================================================//
    List<String> addresses = new ArrayList<String>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9003) {
            if (mLocationPermissionGranted) {
            } else {
                getLocationPermission();
            }
        }

        if (requestCode == 100 & resultCode == RESULT_OK) {

            Place place = Autocomplete.getPlaceFromIntent(data);

            slatlng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            // MarkerOptions markerOptions2 = new MarkerOptions().position(slatlng).title("Your destination");
            //mMap.animateCamera(CameraUpdateFactory.newLatLng(slatlng));
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng,17));
            //mMap.addMarker(markerOptions2);
            Name = place.getName();
            Double latitude1 = slatlng.latitude;
            Double longitude1 = slatlng.longitude;
            editText.setText(place.getAddress());

            if (addresses.size() >= 1) {
                if (!addresses.get(0).isEmpty()) {
                    addresses.remove(0);
                }
            }


            addresses.add(0, place.getName());

            Log.i("Source Address", addresses.toString());

            if (addresses.size() > 1) {
                if (!addresses.get(1).isEmpty()) {
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(slatlng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));

                    radius = 0;
                    keylist.clear();
                    latLngList.clear();
                    fetchParkingSpot();


                }
            } else {

                mMap.addMarker(new MarkerOptions().position(slatlng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng, 15));
            }
           /*DocumentReference documentReference = fStore.collection("Parking Spots").document(Name);
            Map<String,Object> user = new HashMap<>();
            //user.put("Geolocation",slatlng);
            user.put("Name",Name);
            user.put("Latitude",latitude1);
            user.put("Longitude",longitude1);
            user.put("Rate",40);
            user.put("Availabe Slots",20);
            user.put("Total Slots",20);
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(MapsActivity.this, "Parking Location Successfully Stored", Toast.LENGTH_SHORT).show();
                }
                });

            DatabaseReference ref = firebaseDatabase.getInstance().getReference("Parking Spots");
           //REALTIME DATABASE CODE
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(Name,new GeoLocation(place.getLatLng().latitude,place.getLatLng().longitude),new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String Name, DatabaseError error) {
                    if (error != null) {
                        Toast.makeText(MapsActivity.this, "Parking location Successfully stored", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MapsActivity.this, "You can store !" + Name, Toast.LENGTH_SHORT).show();
                }
            }); */
//======================================================================================================FREE PARKING LOCATION STORE CODE ===========================================//
            /*DocumentReference documentReference = fStore.collection("Parking Spots").document(Name);
            Map<String,Object> user = new HashMap<>();
            //user.put("Geolocation",slatlng);
            user.put("Name",Name);
            user.put("Latitude",latitude1);
            user.put("Longitude",longitude1);
            user.put("Rate",0);
            user.put("Availabe Slots",20);
            user.put("Total Slots",20);
            user.put("Open","9:00 AM");
            user.put("Close", "6:00 PM");
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Toast.makeText(MapsActivity.this, "Free Parking Location Successfully Stored", Toast.LENGTH_SHORT).show();
                }
            });

            DatabaseReference ref = firebaseDatabase.getInstance().getReference("Parking Spots");
            //REALTIME DATABASE CODE
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(Name,new GeoLocation(place.getLatLng().latitude,place.getLatLng().longitude),new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String Name, DatabaseError error) {
                    if (error != null) {
                        //Toast.makeText(MapsActivity.this, "Parking location Successfully stored", Toast.LENGTH_SHORT).show();
                    }
                   // Toast.makeText(MapsActivity.this, "You can store !" + Name, Toast.LENGTH_SHORT).show();
                }
            });

*/
//========================================================================================================================================================================================//


        } else if (requestCode == 101 & resultCode == RESULT_OK) {

            Place place = Autocomplete.getPlaceFromIntent(data);

            slatlng1 = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            String Name = place.getName();
            Double latitude1 = slatlng1.latitude;
            Double longitude1 = slatlng1.longitude;
            editText1.setText(place.getAddress());
            if (addresses.size() > 1) {
                if (!addresses.get(1).isEmpty()) {
                    addresses.remove(1);
                }
            }
            addresses.add(1, place.getName());
            Log.i("Dest Address", addresses.toString());

            if (addresses.size() == 2) {
                if (!addresses.get(0).isEmpty()) {
                    mMap.clear();

                    mMap.addMarker(new MarkerOptions().position(slatlng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));


                }
            } else {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));
            }
            radius = 0;
            keylist.clear();
            latLngList.clear();
            fetchParkingSpot();


            //======================================================= DATABASE STORE FIRESTORE==========================================//
            //editText1.setText(place.getAddress());
            /*DocumentReference documentReference = fStore.collection("Parking Spots").document(Name);
            Map<String,Object> user = new HashMap<>();
            user.put("Name",Name);
            user.put("Latitude",latitude1);
            user.put("Longitude",longitude1);
            user.put("Rate",40);
            user.put("Availabe Slots",20);
            user.put("Total Slots",20);
            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(MapsActivity.this, "Parking Location Successfully Stored", Toast.LENGTH_SHORT).show();
                }
            });*/
            //==========================================================================================================================//


            //==================================================== REALTIME DATABASE ==================================================//

           /* DatabaseReference ref = firebaseDatabase.getInstance().getReference("Parking Spots");
            //REALTIME DATABASE CODE
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(Name,new GeoLocation(place.getLatLng().latitude,place.getLatLng().longitude),new GeoFire.CompletionListener() {
              @Override
            public void onComplete(String Name, DatabaseError error) {
              if (error != null) {
                Toast.makeText(MapsActivity.this, "Can't go Active", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(MapsActivity.this, "You are Active" + Name, Toast.LENGTH_SHORT).show();
            }
            });*/

            //=========================================================================================================================//

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    //===========================================================================FREE PARKING SPOT ENABLED==============================================================//

    public void fetchFreeParkingSpot() {

        DatabaseReference parkinglocation = FirebaseDatabase.getInstance().getReference().child("Free Parking Spots");
        GeoFire geoFire = new GeoFire(parkinglocation);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(slatlng1.latitude, slatlng1.longitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("button click", "you are into show locations");
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag().equals(key))
                        return;
                }
                LatLng parkingloc = new LatLng(location.latitude, location.longitude);
                Log.i("Loc", key);
                Log.i("latlang", String.valueOf(parkingloc.longitude) + "," + String.valueOf(parkingloc.latitude));
                latLngList.add(parkingloc);
                keylist.add(key);
            }

            @Override
            public void onKeyExited(String key) {


            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {


            }

            @Override
            public void onGeoQueryReady() {
                if (keylist.isEmpty()) {
                    radius++;

                    Log.i("Radius", String.valueOf(radius));
                    fetchParkingSpot();
                    Log.i("NO PARKING LOCATION", "NO LOCATION FOUND NEARBY");
                    Log.i("GEOKEY", keylist.toString());
                    Log.i("GEOocation", latLngList.toString());
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //=========================================================================================================================================================================================


    //-=============================================================================================RADIUS LOOP =========================================================================/////////

    public void fetchParkingSpot() {

        DatabaseReference parkinglocation = FirebaseDatabase.getInstance().getReference().child("Free Parking Spots");
        GeoFire geoFire = new GeoFire(parkinglocation);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(slatlng1.latitude, slatlng1.longitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("button click", "you are into show locations");
                for (Marker markerIt : markerList) {
                    if (markerIt.getTag().equals(key))
                        return;
                }
                LatLng parkingloc = new LatLng(location.latitude, location.longitude);
                Log.i("Loc", key);
                Log.i("latlang", String.valueOf(parkingloc.longitude) + "," + String.valueOf(parkingloc.latitude));
                latLngList.add(parkingloc);
                keylist.add(key);
            }

            @Override
            public void onKeyExited(String key) {


            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if (keylist.isEmpty()) {
                    radius++;

                    Log.i("Radius", String.valueOf(radius));
                    fetchParkingSpot();
                    Log.i("NO PARKING LOCATION", "NO LOCATION FOUND NEARBY");
                    Log.i("GEOKEY", keylist.toString());
                    Log.i("GEOocation", latLngList.toString());
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        if (!freeParkingEnabled) {
            DatabaseReference parkinglocation1 = FirebaseDatabase.getInstance().getReference().child("Parking Spots");
            GeoFire geoFire1 = new GeoFire(parkinglocation1);

            GeoQuery geoQuery1 = geoFire1.queryAtLocation(new GeoLocation(slatlng1.latitude, slatlng1.longitude), radius);
            geoQuery1.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Log.i("button click", "you are into show locations");
                    for (Marker markerIt : markerList) {
                        if (markerIt.getTag().equals(key))
                            return;
                    }
                    LatLng parkingloc = new LatLng(location.latitude, location.longitude);
                    Log.i("Loc", key);
                    Log.i("latlang", String.valueOf(parkingloc.longitude) + "," + String.valueOf(parkingloc.latitude));
                    latLngList.add(parkingloc);
                    keylist.add(key);
                }

                @Override
                public void onKeyExited(String key) {


                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                }

                @Override
                public void onGeoQueryReady() {
                    if (keylist.isEmpty()) {
                        radius++;

                        Log.i("Radius", String.valueOf(radius));
                        fetchParkingSpot();
                        Log.i("NO PARKING LOCATION", "NO LOCATION FOUND NEARBY");
                        Log.i("GEOKEY", keylist.toString());
                        Log.i("GEOocation", latLngList.toString());
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }


    }

    //=================================================================================== MY LOCATION BUTTON CODE ==========================================================================================//
    public void locationclick(View view) {
        Toast.makeText(MapsActivity.this, "YOU WANT TO SEE YOUR LOCATION HERE YOU GO", Toast.LENGTH_SHORT).show();
        LatLng latlng = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());
//        MarkerOptions markerOptions3 = new MarkerOptions().position(latlng).title("You are here");
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,17));
//        mMap.addMarker(markerOptions3);
    }
//=====================================================================================ADDING MARKERS OF NEARBY PARKING SPACES ================================================================================================//

    List<Marker> markerList = new ArrayList<Marker>();
    List<LatLng> latLngList = new ArrayList<LatLng>();
    List<String> keylist = new ArrayList<String>();

    public void Showlocation(View view) {
        mMap.clear();
        if (addresses.size() == 2) {
            if (!addresses.get(0).isEmpty()) {
                mMap.clear();

                mMap.addMarker(new MarkerOptions().position(slatlng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));


            }
        } else {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(slatlng1).title(addresses.get(1)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(slatlng1, 15));
        }
        for (int i = 0; i < keylist.size(); i++) {
            Log.i("ADDING MARKER ", "MARKER NAME " + keylist.get(i));
            mMap.addMarker(new MarkerOptions().position(latLngList.get(i)).title(keylist.get(i)).icon(BitmapDescriptorFactory.fromResource(R.drawable.paid_park_black)));
//            mMap.addMarker(new MarkerOptions().position(l2).title("Cooper Hospital OPD").icon(BitmapDescriptorFactory.fromResource(R.drawable.free_park_black)));

//            MarkerOptions markerOptions4 = new MarkerOptions().position(latLngList.get(i)).title(keylist.get(i));
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngList.get(i)));
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(i),17));
//            mMap.addMarker(markerOptions4).setTag(keylist.get(i));
        }

    }

    //=========================================================================FETCHING DATA FROM MARKER CLICK ========================================================================================//
    TextView slots, rate, distance;
    List<String> key, value;
    String tag,rte;

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i("Marker Clicked", marker.getTitle());
        boolean srcOrDest;

        String s = marker.getTitle(), s1 = addresses.get(0), s2 = addresses.get(1);
        boolean c1 = s.equals(s1), c2 = s.equals(s2);
        Log.i("Markers", s2 + " && " + s1 + " && " + s);
        if (c1) {
            Log.i("Markers", s1 + " && " + s);
            srcOrDest = true;
        } else if (c2) {
            Log.i("Markers", s2 + " && " + s);
            srcOrDest = true;
        } else {
            srcOrDest = false;
        }


        Log.i("Marker Clicked", String.valueOf((srcOrDest)));

        if (!srcOrDest) {
            //initializing
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapsActivity.this);
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);
            //initializing content
            slots = (TextView) bottomSheetDialog.findViewById(R.id.slots);
            rate = (TextView) bottomSheetDialog.findViewById(R.id.rate);
            distance = (TextView) bottomSheetDialog.findViewById(R.id.distance);
            //setting data from database
            tag = marker.getTitle();
            key = new ArrayList<String>();
            value = new ArrayList<String>();
            if (String.valueOf(rate) == "0") {


                Button bookNow = bottomSheetDialog.findViewById(R.id.bookNow);
                bookNow.setVisibility(View.GONE);

            }
            try {

                DocumentReference fetchdata2 = fStore.collection("Free Parking Spots").document(tag);
                fetchdata2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> datafirestore = documentSnapshot.getData();

                            rte = String.valueOf(documentSnapshot.getLong("Rate"));
                            rate.setText(rte);
                            String slts = String.valueOf(documentSnapshot.getLong("Availabe Slots"))+ "/" + String.valueOf(documentSnapshot.getLong("Total Slots"));
                            slots.setText(slts);

//                            String temp;
//                            for (Map.Entry<String, Object> entry : datafirestore.entrySet()) {
//                                String k = entry.getKey();
//                                String v = String.valueOf(entry.getValue());
//                                key.add(k);
//                                value.add(v);
//                            }
//                            Log.i("MESSAGE", key.toString() + " ," + value.toString());
//                            rate.setText(value.get(0));
//
//                            temp = value.get(1) + "/" + value.get(5);
//                            slots.setText(temp);
//                            Log.i("RATE AND TEMP ", value.get(0) + temp);
                        } else {
//                                Toast.makeText(MapsActivity.this, "DOCUMENT DOES NOT EXITS", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                if (!freeParkingEnabled) {
                    DocumentReference fetchdata3 = fStore.collection("Parking Spots").document(tag);
                    fetchdata3.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Map<String, Object> datafirestore = documentSnapshot.getData();

                                rte = String.valueOf(documentSnapshot.getLong("Rate"));
                                rate.setText(rte);
                                String slts = String.valueOf(documentSnapshot.getLong("Availabe Slots"))+ "/" + String.valueOf(documentSnapshot.getLong("Total Slots"));
                                slots.setText(slts);

//                                String temp;
//                                for (Map.Entry<String, Object> entry : datafirestore.entrySet()) {
//                                    String k = entry.getKey();
//                                    String v = String.valueOf(entry.getValue());
//                                    key.add(k);
//                                    value.add(v);
//                                }
//                                Log.i("MESSAGE", key.toString() + " ," + value.toString());
//                                rate.setText(value.get(0));
//
//                                temp = value.get(1) + "/" + value.get(5);
//                                slots.setText(temp);
//                                Log.i("RATE AND TEMP ", value.get(0) + temp);
                            } else {
//                               Toast.makeText(MapsActivity.this, "DOCUMENT DOES NOT EXITS", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            //for calculating distance
            float result[] = new float[10];
            Location.distanceBetween(slatlng1.latitude, slatlng1.longitude, marker.getPosition().latitude, marker.getPosition().longitude, result);
            Log.i("Distance", result.toString());
            distance.setText(String.valueOf(Math.floor((result[0] / 1000) * 100) / 100) + "km");
            //Toast.makeText(MapsActivity.this,String.valueOf(result[0]), Toast.LENGTH_SHORT).show();
            Log.i("Distance", String.valueOf(result[0]));
            Log.i("MArker cord", marker.getPosition().toString());


            Button bookNow = bottomSheetDialog.findViewById(R.id.bookNow);
            bookNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (rte.equals("0")) {
                        String add0 = addresses.get(0).trim().replace(" ", "+"), add1 = marker.getTitle().trim().replace(" ", "+");
                        String sa = "google.navigation:q=" + add0 + ",+" + add1 + "&avoid=tf";
                        Intent intent = new Intent(getApplicationContext(), FreeParkSpot.class);
                        intent.putExtra("link", sa);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), BookNowPage.class);
                        String add0 = addresses.get(0).trim().replace(" ", "+"), add1 = marker.getTitle().trim().replace(" ", "+");
                        intent.putExtra("add0", add0);
                        intent.putExtra("add1", add1);
                        intent.putExtra("Parking Location Name", tag);
                        intent.putExtra("Rate", rte);
                        startActivity(intent);
                    }
                }
            });
            //button click functionality
            Button button = bottomSheetDialog.findViewById(R.id.startNavigation);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String add0 = addresses.get(0).trim().replace(" ", "+"), add1 = marker.getTitle().trim().replace(" ", "+");
                    Log.i("navigate",add1);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + add0 + ",+" + add1 + "&avoid=tf");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                    //Toast.makeText(MapsActivity.this,"Its working",Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.cancel();
                }
            });
            bottomSheetDialog.show();
        }
        return false;
    }


    public void fetchdata(View view) {

        DocumentReference fetchdata2 = fStore.collection("Parking Spots").document("Borivali");
        fetchdata2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> datafirestore = documentSnapshot.getData();

                    for (Map.Entry<String, Object> entry : datafirestore.entrySet()) {
                        String k = entry.getKey();
                        String v = String.valueOf(entry.getValue());
                        Log.i("DOCUMENT INFO ", "Key: " + k + ", Value: " + v);
                    }

                } else {

                    Toast.makeText(MapsActivity.this, "DOCUMENT DOES NOT EXITS", Toast.LENGTH_SHORT).show();

                }


            }
        });
    }


//=================================================================================================================================================================================================//


//===========================================================================================CURRENT LOCATION CODE ==================================================================================//

    public void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentlocation = location;
                    //Toast.makeText(getApplicationContext(),currentlocation.getLatitude()+""+currentlocation.getLongitude(),Toast.LENGTH_SHORT).show();
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsActivity.this);

                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.mapstyle));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }

        //
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
       googleMap.setPadding(0,40,30,100);
        googleMap.setTrafficEnabled(false);
        googleMap.setBuildingsEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng latlng = new LatLng(currentlocation.getLatitude(),currentlocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,15));

        //for setting default location to my location in source edit text

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(currentlocation.getLatitude(), currentlocation.getLongitude(), 1);
            if (listAddresses != null && listAddresses.size() > 0) {
                String address = "";
                if (listAddresses.get(0).getAddressLine(0) != null) {
                    address = listAddresses.get(0).getAddressLine(0);
                    LatLng latLng = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                    String[] temp = address.split(", ");
                    Log.i("address",Arrays.toString(temp));

                    int i = 0;
                    while(temp[i]!= null) {
                        Boolean numeric = false;
                        if(temp[i].matches("[0-9]+")) {
                            numeric = true;
                        }
                        if (!numeric) {
                            yourlocation = temp[i];
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }
}
//====================================================================================================================================================================================================================================