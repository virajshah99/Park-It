package com.example.parkit;

import androidx.annotation.NonNull;
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
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkit.DirectionHelper.TaskLoadedCallback;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        TaskLoadedCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener{
        //GoogleMap.OnMarkerDragListener{

    private GoogleMap mMap;
    private PlacesClient placesClient;
    //old method
    //private List<AutocompletePrediction> predictionList;
    //AutoCompleteTextView sourceEditText,destinationEditView;
    //List<String> suggestionList;
    //ArrayAdapter<String> adapter;

    Polyline polyline = null;
    List<String> addresses =  new ArrayList<>();
    LatLng sLatLng,dLatLng;

    private boolean mLocationPermissionGranted = false;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_code = 101;

    //new places method
    AutoCompleteTextView sourceEditText,destEditText;
    String yourLoc, name,email,phoneNo,password;

    //Variables for drawer Layout
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    //Toolbar toolbar;
    Button navButton;
    boolean freeParkingEnabled = false;
    DatabaseReference userReference,paidParkReference;

    double radius = 0;
    List<Marker> markerList = new ArrayList<Marker>();
    List<LatLng> latLngList = new ArrayList<LatLng>();
    List<String> keylist = new ArrayList<String>();




    //requesting permissions
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }
    //for checking net service
    private boolean isNetConnected(){
        ConnectivityManager cm =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected) {
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
    public boolean isMapsEnabled(){
        LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
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
    private void getLocationPermission(){
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
    public boolean isServicesOK(){
        Log.d("TAG", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d("TAG", "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d("TAG", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, 9001);//ERROR_DIALOG_REQUEST
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //location services app install permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
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
    public void fetchLastLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    //button  click function
    public void userLocation(View view){
        try{
        LatLng curLoc = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curLoc,15));
    }catch (Exception e){
            System.out.println(e);
        }
    }

    public void navButtonPressed(View view){
        //hooks for drawer layout
        drawerLayout=findViewById(R.id.drawerLayout);
        navigationView=findViewById(R.id.nav_view);
        navButton = (Button)findViewById(R.id.navButton);
        drawerLayout.openDrawer(GravityCompat.START);

        //navigationView.bringToFront();
        setupDrawerContent(navigationView);
    }

    //activity when item is selected in nav drawer
    private void setupDrawerContent(NavigationView navigationView1){
        navigationView1.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                switch (item.getItemId()){
                    case R.id.nav_home:
                        break;
                    case R.id.nav_profile:
                        Intent intent = new Intent(getApplicationContext(), profile.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_logout:
                        new androidx.appcompat.app.AlertDialog.Builder(MapsActivity.this)
                                .setIcon(R.drawable.ic_error)
                                .setTitle("Are you sure you want to Logout")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                                        sharedPreferences.edit().putString("name","").apply();
                                        sharedPreferences.edit().putString("email","").apply();
                                        sharedPreferences.edit().putString("phoneNo","").apply();
                                        sharedPreferences.edit().putString("password","").apply();
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    //for nav drawer back button functionality
    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    //sets filter to search free parking or both
    public void filterParkingResult(View view){
        Button button = (Button) findViewById(R.id.freeParkOnly);
        String c = button.getText().toString();
        if(c == "PF"){
            button.setText("F");
            freeParkingEnabled = true;
        }else{
            button.setText("PF");
            freeParkingEnabled = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Places.initialize(MapsActivity.this,getString(R.string.google_maps_key));
        //placesClient = Places.createClient(this);
        //final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //enabling location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        //checking services
        checkMapServices();
        isNetConnected();

        userReference = FirebaseDatabase.getInstance().getReference("user");
        SharedPreferences  sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name","");

        //for preforming activity when nav drawer is opened by slide method
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView =findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        TextView headerName = navigationView.getHeaderView(0).findViewById(R.id.headerName);
        headerName.setText(name.toUpperCase());

        this.setupDrawerContent(navigationView);

//        if(getIntent().hasExtra("phoneNo")){
//
//            name = getIntent().getStringExtra("name");
//            email = getIntent().getStringExtra("email");
//            phoneNo = getIntent().getStringExtra("phoneNo");
//            password = getIntent().getStringExtra("password");
//
//            sharedPreferences.edit().putString("name",name).apply();
//            sharedPreferences.edit().putString("email",email).apply();
//            sharedPreferences.edit().putString("phoneNo",phoneNo).apply();
//            sharedPreferences.edit().putString("password",password).apply();
//
//            TextView headerName = navigationView.getHeaderView(0).findViewById(R.id.headerName);
//            headerName.setText(name.toUpperCase());
//            Log.i("headername", name);
//
//        }
//        String username = sharedPreferences.getString("username","");
//        Log.i("username",username);


        //start of code for fetching places using places api
        //initializing using het's key
        Places.initialize(getApplicationContext(),"AIzaSyCbNQKZqT1myNkOfJiDCBe2jGEQamf52dQ");
        placesClient = Places.createClient(this);


        sourceEditText = (AutoCompleteTextView) findViewById(R.id.sourceAutoCompleteTextView);
        sourceEditText.setFocusable(false);
        sourceEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldlist = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                String temp = sourceEditText.getText().toString();
                if(temp.isEmpty()){
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").setInitialQuery(yourLoc).build(MapsActivity.this);
                    startActivityForResult(intent, 200);
                }else{
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").setInitialQuery(temp).build(MapsActivity.this);
                    startActivityForResult(intent, 200);
                }
            }
        });

        destEditText = (AutoCompleteTextView) findViewById(R.id.destinationAutoCompleteTextView);
        destEditText.setFocusable(false);
        destEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldlist = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                String temp = destEditText.getText().toString();
                if(!temp.isEmpty()){
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").setInitialQuery(temp).build(MapsActivity.this);
                    startActivityForResult(intent, 201);
                }else{
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldlist).setCountry("IN").build(MapsActivity.this);
                    startActivityForResult(intent, 201);
                }
            }
        });

    /*======================================================= OLD PLACES API METHOD =======================================================*/
//======================================================= FOR SOURCE =======================================================//
//        suggestionList = new ArrayList<>();
//        suggestionList.add("Your Location");
//        sourceEditText = (AutoCompleteTextView) findViewById(R.id.sourceAutoCompleteTextView);
//        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,suggestionList);
//        sourceEditText.setAdapter(adapter);
//
//
//        sourceEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                //searchBtn.setCompoundDrawablesWithIntrinsicBounds(com.example.parkit.R.drawable.ic_close_black_48dp, 0, 0, 0);
//
//                final FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
//                        .setCountry("IN")
//                        .setTypeFilter(TypeFilter.ADDRESS)
//                        .setSessionToken(token)
//                        .setQuery(s.toString())
//                        .build();
//                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
//
//                        if (task.isSuccessful()) {
//                            Log.i("Fetching Places", "Places Fetching ");
//                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
//                            if (predictionsRequest != null) {//&& predictionsResponse != null){
//                                Log.i("Fetching Places", "Places Fetching successful");
//                                predictionList = predictionsResponse.getAutocompletePredictions();
//                                suggestionList = new ArrayList<>();
//                                //suggestionList.add("Your Location");
//                                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, suggestionList);
//                                for (int i = 0; i < predictionList.size(); i++) {
//                                    AutocompletePrediction prediction = predictionList.get(i);
//                                    suggestionList.add(prediction.getFullText(null).toString());
//                                    Log.i("Fetching Places", "Places Fetched" + predictionList.size());
//                                }
//                                //adapter.notifyDataSetChanged();
//                                sourceEditText.setAdapter(adapter);
//                                sourceEditText.showDropDown();
//                            } else
//                                Log.i("Fetching Places", "Places Fetching unsuccessful");
//                        } else {
//                            Log.i("Error Fetching Places", "Places Fetching unsuccessful");
//                        }
//                    }
//                });
//            }
//
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//
//        sourceEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
//                    Log.i("Location Not Found", "Button Clicked");
//                    String address = sourceEditText.getText().toString();
//                    List<Address> addressList = null;
//                    if(!TextUtils.isEmpty(address)){
//                        Geocoder geocoder = new Geocoder(getApplicationContext());
//                        try {
//                            addressList = geocoder.getFromLocationName(address,6);
//                            if(addressList != null) {
//                                mMap.clear();
//                                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                                imm.hideSoftInputFromWindow(sourceEditText.getWindowToken(), 0);
//                                for (int i = 0; i < addressList.size(); i++) {
//                                    Address userAddress = addressList.get(i);
//                                    sLatLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
//
//
//                                    if(addresses.size()>=1){
//                                        addresses.remove(0);
//                                        addresses.add(0,userAddress.getAdminArea());
//                                    }
//
//                                    if(addresses.size()>1){
//                                        if (!addresses.get(1).isEmpty()){
//                                            mMap.clear();
//                                            mMap.addMarker(new MarkerOptions().position(sLatLng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//                                            mMap.addMarker(new MarkerOptions().position(dLatLng).title(addresses.get(1)));
//                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatLng,15));
//                                        }
//                                    }else{
//                                         mMap.addMarker(new MarkerOptions().position(sLatLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//                                         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sLatLng, 15));
//                                    }
//                                    /*MarkerOptions markerOptions =  new MarkerOptions().position(latLng).title(userAddress.getAddressLine(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                                    Marker marker = mMap.addMarker(markerOptions);
//                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                                    */
//                                    //for polyline
//                                    //latlngList.add(0,latLng);
//                                    //markerList.add(0,marker);
//                                    //markerOptionsList.add(0,markerOptions);
//                                }
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }else{
//                        Toast.makeText(MapsActivity.this,"Location Not Found", Toast.LENGTH_SHORT).show();
//                        Log.i("Location Not Found", "None");
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//

//======================================================= FOR DESTINATION =======================================================
//
//        suggestionList = new ArrayList<>();
//        suggestionList.add("Your Location");
//        destinationEditView = (AutoCompleteTextView) findViewById(R.id.destinationAutoCompleteTextView);
//        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,suggestionList);
//        destinationEditView.setAdapter(adapter);
//
//        destinationEditView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                //searchBtn.setCompoundDrawablesWithIntrinsicBounds(com.example.parkit.R.drawable.ic_close_black_48dp, 0, 0, 0);
//
//                final FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
//                        .setCountry("IN")
//                        .setTypeFilter(TypeFilter.ADDRESS)
//                        .setSessionToken(token)
//                        .setQuery(s.toString())
//                        .build();
//                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
//
//                        if (task.isSuccessful()) {
//                            Log.i("Fetching Places", "Places Fetching ");
//                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
//                            if (predictionsRequest != null) {//&& predictionsResponse != null){
//                                Log.i("Fetching Places", "Places Fetching successful");
//                                predictionList = predictionsResponse.getAutocompletePredictions();
//                                suggestionList = new ArrayList<>();
//                                //suggestionList.add("Your Location");
//                                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, suggestionList);
//                                for (int i = 0; i < predictionList.size(); i++) {
//                                    AutocompletePrediction prediction = predictionList.get(i);
//                                    suggestionList.add(prediction.getFullText(null).toString());
//                                    Log.i("Fetching Places", "Places Fetched" + predictionList.size());
//                                }
//                                //adapter.notifyDataSetChanged();
//                                destinationEditView.setAdapter(adapter);
//                                destinationEditView.showDropDown();
//                            } else
//                                Log.i("Fetching Places", "Places Fetching unsuccessful");
//                        } else {
//                            Log.i("Error Fetching Places", "Places Fetching unsuccessful");
//                        }
//                    }
//                });
//            }
//
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        destinationEditView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
//                    Log.i("Location Not Found", "Button Clicked");
//                    String address = destinationEditView.getText().toString();
//                    List<Address> addressList = null;
//                    if(!TextUtils.isEmpty(address)){
//                        Geocoder geocoder = new Geocoder(getApplicationContext());
//                        try {
//                            addressList = geocoder.getFromLocationName(address,6);
//                            if(addressList != null) {
//                                //mMap.clear();
//                                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                                imm.hideSoftInputFromWindow(destinationEditView.getWindowToken(), 0);
//                                for (int i = 0; i < addressList.size(); i++) {
//                                    Address userAddress = addressList.get(i);
//                                    dLatLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
//
//
//                                    if(addresses.size()>1){
//                                        addresses.remove(1);
//                                        addresses.add(1,userAddress.getAdminArea());
//                                    }
//
//                                    if(addresses.size()>1){
//                                        if (!addresses.get(0).isEmpty()){
//                                            mMap.clear();
//                                            mMap.addMarker(new MarkerOptions().position(sLatLng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//                                            mMap.addMarker(new MarkerOptions().position(dLatLng).title(addresses.get(1)));
//                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatLng,15));
//                                        }
//                                    }else{
//                                        mMap.addMarker(new MarkerOptions().position(dLatLng).title(address));
//                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatLng, 15));
//                                    }
//
//                                    /*MarkerOptions markerOptions =  new MarkerOptions().position(latLng).title(userAddress.getAddressLine(0));
//                                    Marker marker = mMap.addMarker(markerOptions);
//                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));*/
//
//                                    //for polyline
//
//                                    //latlngList.add(1,latLng);
//                                    //markerList.add(1,marker);
//                                    //markerOptionsList.add(1,markerOptions);
//                                }
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        //for calculating distance
//                       /* float result[] = new float[10];
//                        Location.distanceBetween(sLatLng.latitude,sLatLng.longitude,dLatLng.latitude,dLatLng.longitude,result);
//                        Toast.makeText(MapsActivity.this,String.valueOf(result[0]), Toast.LENGTH_SHORT).show();
//                        Log.i("Distance", String.valueOf(result[0]));*/
//
///*                      //For  getting directions
//
//                        String url = getUrl(latlngList.get(0),latlngList.get(1),"driving");
//                        new FetchURL(MapsActivity.this).execute(url, "driving");
//*/
//
//
//                    }else{
//                        Toast.makeText(MapsActivity.this,"Location Not Found", Toast.LENGTH_SHORT).show();
//                        Log.i("Location Not Found", "None");
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG", "onActivityResult: called.");
        if(requestCode == 9003){
            if(!mLocationPermissionGranted){
                getLocationPermission();
            }
        }
        //source marker
        if (requestCode == 200 & resultCode == RESULT_OK) {

            Place place = Autocomplete.getPlaceFromIntent(data);

            sLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            //for adding marker

            if(addresses.size()>=1){
                addresses.remove(0);
                addresses.add(0,place.getName());
            }else if(addresses.size() == 0){
                addresses.add(place.getName());
            }
            if(addresses.size()>1){
                if (!addresses.get(1).isEmpty()){
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(sLatLng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.addMarker(new MarkerOptions().position(dLatLng).title(addresses.get(1)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatLng,15));
                }
            }else{
                mMap.addMarker(new MarkerOptions().position(sLatLng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.free_park_white1)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sLatLng, 15));
            }

            sourceEditText.setText(place.getName() + ", " + place.getAddress());

        }else if (requestCode == 201 & resultCode == RESULT_OK) {

            Place place = Autocomplete.getPlaceFromIntent(data);

            dLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            //for adding marker
            if(addresses.size()>1){
                addresses.remove(1);
                addresses.add(1,place.getName());
            }else if(addresses.size() <= 1){
                addresses.add(place.getName());
            }
            if(addresses.size()>1){
                if (!addresses.get(0).isEmpty()){
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(sLatLng).title(addresses.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.addMarker(new MarkerOptions().position(dLatLng).title(addresses.get(1)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatLng,15));
                }
            }else{
                mMap.addMarker(new MarkerOptions().position(dLatLng).title(place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dLatLng, 15));
            }

            destEditText.setText(place.getName() + ", " + place.getAddress());

            radius = 0;
            keylist.clear();
            latLngList.clear();
            fetchParkingSpot();

        }else if (resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    public void fetchParkingSpot(){

        /*paidParkReference = FirebaseDatabase.getInstance().getReference().child("Paid Parking Spots");
        GeoFire geoFire = new GeoFire(paidParkReference);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(dLatLng.latitude,dLatLng.longitude),radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("button click","you are into show locations");
                for(Marker markerIt : markerList){
                    if(markerIt.getTag().equals(key))
                        return;
                }
                LatLng parkingloc = new LatLng(location.latitude,location.longitude);
                Log.i("Loc",key);
                Log.i("latlang",String.valueOf(parkingloc.longitude));
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
                if(keylist.isEmpty()) {
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
        });*/
        LatLng l1 = new LatLng(19.1077981,72.83950089999999);
        LatLng l2 = new LatLng(19.1077239,72.8354382);
        mMap.addMarker(new MarkerOptions().position(l1).title("Prime Mall").icon(BitmapDescriptorFactory.fromResource(R.drawable.paid_park_black)));
        mMap.addMarker(new MarkerOptions().position(l2).title("Cooper Hospital OPD").icon(BitmapDescriptorFactory.fromResource(R.drawable.free_park_black)));
    }

    public void Showlocation(View view) {
        for(int i = 0;i<keylist.size();i++){
            Log.i("ADDING MARKER ","MARKER NAME "+ keylist.get(i));
            MarkerOptions markerOptions4 = new MarkerOptions().position(latLngList.get(i)).title(keylist.get(i));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngList.get(i)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(i),10));
            mMap.addMarker(markerOptions4);
        }

    }

    //for directions
/*
    //for drawing routes
    private String getUrl(LatLng origin, LatLng dest, String directionMode){
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyCbNQKZqT1myNkOfJiDCBe2jGEQamf52dQ";
        return url;
    }
*/

    //map clicked puts marker on it
    public void onMapClick(LatLng location){
        /*Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (listAddresses != null && listAddresses.size() > 0) {
                String address = "";
                if (listAddresses.get(0).getAddressLine(0) != null) {
                    address = listAddresses.get(0).getAddressLine(0) + " ";
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(location).title(address));
                }
            }
        } catch (Exception e) {
            Log.i("Error", e.toString());
        }*/
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
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setBuildingsEnabled(true);

        mMap.setOnMarkerClickListener(this);

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
        if(currentLocation!= null) {
            //for defining user location in source box
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                if (listAddresses != null && listAddresses.size() > 0) {
                    String address = "";
                    if (listAddresses.get(0).getAddressLine(0) != null) {
                        address = listAddresses.get(0).getAddressLine(0);
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        String[] temp = address.split(", ");
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

    //draws poly line when data is fetched
    @Override
    public void onTaskDone(Object... values){
        if(polyline != null)
            polyline.remove();
        polyline = mMap.addPolyline((PolylineOptions)values[0]);
    }

    //For adding location into data base ---- For inserting data purpose
    //when clicked on marker title window
    @Override
    public void onInfoWindowClick(Marker marker){
        /*if(marker.getSnippet().equals("Marker on Mumbai")){
            marker.hideInfoWindow();
        }
        else{*/
            Toast.makeText(getApplicationContext(),"Showing",Toast.LENGTH_SHORT).show();
            Log.i("MSG","THIS*************************8");
            new androidx.appcompat.app.AlertDialog.Builder(MapsActivity.this)
                    .setIcon(android.R.drawable.alert_dark_frame)
                    .setTitle("Alert")
                    .setMessage("Is this location correct?\nDo you want to add it in database?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(MapsActivity.this,"Its done",Toast.LENGTH_LONG).show();


                            //Enter code here
                        }
                    })
                    .setNegativeButton("No",null)
                    .show();

           /*
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage(marker.getSnippet())
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            //Add code over here
                            //calculateDirections(marker);






                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
*/
        //}
    }

    //for details about marker like dist form dest, rate, availabe slots, etc
    @Override
    public boolean onMarkerClick(Marker marker){
        Log.i("Marker Clicked", marker.getTitle());
        boolean srcOrDest = false;

        /*String s = marker.getTitle(),s1 = addresses.get(0),s2 = addresses.get(1);
        boolean c1=s.equals(s1),c2 = s.equals(s2);
        Log.i("Markers", s2+" && "+s1 + " && " + s );
        if(c1) {
            Log.i("Markers", s1 + " && " + s);
            srcOrDest = true;
        }else if(c2){
            Log.i("Markers", s2+ " && " + s);
            srcOrDest = true;
        }else{
            srcOrDest = false;
        }*/

        if( !srcOrDest ){
            //initializing
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapsActivity.this);
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);
            //initializing content
            TextView slots = (TextView) bottomSheetDialog.findViewById(R.id.slots);
            TextView rate = (TextView)bottomSheetDialog.findViewById(R.id.rate);
            TextView distance = (TextView)bottomSheetDialog.findViewById(R.id.distance);
            //setting data from database
            slots.setText("18/20");
            rate.setText("Rs. 40");
            //for calculating distance
            float result[] = new float[10];
            Location.distanceBetween(dLatLng.latitude,dLatLng.longitude,marker.getPosition().latitude,marker.getPosition().longitude,result);
            distance.setText(String.valueOf(Math.floor((result[0]/1000) * 100) / 100) + "km");
            //Toast.makeText(MapsActivity.this,String.valueOf(result[0]), Toast.LENGTH_SHORT).show();
            //Log.i("Distance", String.valueOf(result[0]));

            Button bookNow = bottomSheetDialog.findViewById(R.id.bookNow);
            bookNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), BookNowPage.class);
                    startActivity(intent);
                }
            });
            //button click functionality
            Button button = bottomSheetDialog.findViewById(R.id.startNavigation);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //For  getting directions
//                String url = getUrl(sLatLng, marker.getPosition(),"driving");
//                  new FetchURL(MapsActivity.this).execute(url, "driving");

                    String add0 = addresses.get(0).trim().replace(" ","+"),add1 = addresses.get(1).trim().replace(" ","+");

                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + add0 + ",+" + add1 +"&avoid=tf");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);

                    Toast.makeText(MapsActivity.this,"Its working",Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.cancel();
                }
            });
            bottomSheetDialog.show();
        }
        return false;
    }
}

//For marker drag activity watch video: https://www.youtube.com/watch?v=9V0p_2lVoJo

//for polyline maker list , latlng list

//available slot, total, rate, distance form dest