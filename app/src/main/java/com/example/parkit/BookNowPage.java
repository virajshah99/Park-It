package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BookNowPage extends AppCompatActivity implements TimePickerFragment.TimePickerListener, AdapterView.OnItemSelectedListener {

    private TextView starttime,stoptime;
    String sourceaddress,destinationaddress,stime,ltime,fueltype,parkinglocname,rate,userunique,carno;
    EditText nameTextBox,carnumber;
    FirebaseUser user;
    Spinner spinnerFuel;
    FirebaseFirestore fstore;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    Boolean timepick1=false,timepick2=false;
    String formattedDate;
    float hr,mini;
    long revenue,slot;
    List<String> key,value;
    long difference = 0,diffHours,diffMinutes;
    public void book(View view){
        TextView t1 = findViewById(R.id.startTime);

        TextView t2 = findViewById(R.id.leaveTime);

        carnumber = (EditText) findViewById(R.id.Carnumberplate);

        String time1 = t1.getText().toString();
        String time2 = t2.getText().toString();
        carno = carnumber.getText().toString();
        try {
            SimpleDateFormat format = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                format = new SimpleDateFormat("HH:mm:ss");
                Date date1 = format.parse(time1 + ":00");
                Date date2 = format.parse(time2 + ":00");
                difference = date2.getTime() - date1.getTime();
                diffHours = difference / (60 * 60 * 1000) % 24;
                diffMinutes = (difference / (60 * 1000) % 60);
                Log.i("iTotal Min", String.valueOf(diffMinutes));
                mini = (float)diffMinutes/60;
                hr = (float)diffHours+mini;
            }
        } catch(Exception e) {
        }

        float r = Integer.valueOf(rate)*hr;

        if(carnumber==null || stime ==null || ltime == null || fueltype == null || difference < 1800000){

            Toast.makeText(BookNowPage.this, "Please Enter Missing Details",Toast.LENGTH_SHORT).show();

            Log.i("ISource Address",sourceaddress);
            Log.i("iDestination Address",destinationaddress);
            Log.i("iStart Time",stime);
            Log.i("iStop Time",ltime);
            Log.i("iFuel Type",spinnerFuel.getSelectedItem().toString());
            Log.i("iCost",rate);
            Log.i("iTotal Cost", String.valueOf(r));
            Log.i("iTotal Cost", String.valueOf(hr));
        }
        else {
            revenue = (long)r + revenue;
            Log.i("Revenue Total",String.valueOf(revenue));

            Map<String,Object> use = new HashMap<>();
            //use.put("Geolocation",slatlng);
            Log.i("Source Address",sourceaddress.replace("+"," "));
            Log.i("Destination Address",destinationaddress.replace("+"," "));
            Log.i("Start Time",stime);
            Log.i("Stop Time",ltime);
            Log.i("Fuel Type",spinnerFuel.getSelectedItem().toString());
            Log.i("Cost",rate);
            Log.i("Total Cost", String.valueOf((long)r));
            Log.i("Car no", carno);
            Log.i("iTotal Cost", String.valueOf(hr));
//
            use.put("Source Address",sourceaddress.replace("+"," "));
            use.put("Destination Address",destinationaddress.replace("+"," "));
            use.put("Start Time",stime);
            use.put("Stop Time",ltime);
            use.put("Fuel Type",spinnerFuel.getSelectedItem().toString());
            use.put("Total Cost", (long) r);
            use.put("Car Number",carno);

            fstore.collection("Parking Spots").document(parkinglocname).collection("Bookings").document(userunique+" "+formattedDate)
                    .set(use)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error writing document", e);
                        }
                    });

            DocumentReference documentReference1 = fstore.collection("Parking Spots").document(parkinglocname);
            documentReference1
                    .update("Revenue", revenue)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("RSuccess", "revenue successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("err", "Error updating document", e);
                        }
                    });
            DocumentReference documentReference2 = fstore.collection("Parking Spots").document(parkinglocname);
            documentReference2
                    .update("Availabe Slots", slot-1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("sSuccess", "slots successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("err", "Error updating document", e);
                        }
                    });

            Map<String, Object> city = new HashMap<>();
            city.put("Source Address",sourceaddress.replace("+"," "));
            city.put("Destination Address",destinationaddress.replace("+"," "));
            city.put("Start Time",stime);
            city.put("Stop Time",ltime);
            city.put("Fuel Type",spinnerFuel.getSelectedItem().toString());
            city.put("Total Cost",(long)r);
            city.put("Car Number",carno);

            fstore.collection("Users").document(userunique).collection("Booking History").document(formattedDate)
                    .set(city)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Book History", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Book History", "Error writing document", e);
                        }
                    });

           /* DocumentReference documentReference2 = fstore.collection("Parking Spots").document(parkinglocname);
            documentReference2
                    .update("", revenue)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Success", "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("err", "Error updating document", e);
                        }
                    });*/

//            Toast.makeText(BookNowPage.this,carno,Toast.LENGTH_LONG).show();
            carno = carnumber.getText().toString();

            Intent intent = new Intent(getApplicationContext(), qr_code.class);
            intent.putExtra("add0", sourceaddress);
            intent.putExtra("Carno",carno);
            intent.putExtra("stime", stime);
            intent.putExtra("ltime",ltime);
            intent.putExtra("parkinglocname",parkinglocname);
            intent.putExtra("add1", destinationaddress);
            startActivity(intent);

        }
    }

    public void getTime(View view){
        timepick1 = true;
        timepick2 = false;
        starttime = findViewById(R.id.startTime);
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setCancelable(false);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
        //Log.i("getTime",String.valueOf(timePickerFragment.getId()));

    }
    public void getTime1(View view){
        timepick1 = false;
        timepick2 = true;
        stoptime = findViewById(R.id.leaveTime);
        DialogFragment timePickerFragment1 = new TimePickerFragment();
        timePickerFragment1.setCancelable(false);
        timePickerFragment1.show(getSupportFragmentManager(), "timePicker1");
    }

    int aspot;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_now_page);
        getSupportActionBar().hide();
        spinnerFuel = (Spinner) findViewById(R.id.spinnerFuel);
        firebaseDatabase = FirebaseDatabase.getInstance();
        carnumber = (EditText) findViewById(R.id.Carnumberplate);
        parkinglocname = getIntent().getStringExtra("Parking Location Name");
        rate = getIntent().getStringExtra("Rate");
        sourceaddress = getIntent().getStringExtra("add0");
        destinationaddress = getIntent().getStringExtra("add1");

        fstore = FirebaseFirestore.getInstance();
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formattedDate = df.format(c.getTime());

        //for cars
        spinnerFuel.setOnItemSelectedListener(this);
        Log.i("Spinner tag", spinnerFuel.getSelectedItem().toString());
        fueltype = spinnerFuel.getSelectedItem().toString();
        // Setting User Name
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();



        key = new ArrayList<String>();
        value = new ArrayList<String>();
        DocumentReference fetchdata2 = fstore.collection("Parking Spots").document(parkinglocname);
        fetchdata2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> datafirestore = documentSnapshot.getData();

                    revenue = documentSnapshot.getLong("Revenue");
                    Log.i("Revenue", String.valueOf(revenue));
                    slot = documentSnapshot.getLong("Availabe Slots");
                    Log.i("Revenue", String.valueOf(slot));
//                    String temp;
//                    for (Map.Entry<String, Object> entry : datafirestore.entrySet()) {
//                        String k = entry.getKey();
//                        String v = String.valueOf(entry.getValue());
//                        key.add(k);
//                        value.add(v);
//                    }
//                    Log.i("MESSAGE", key.toString() + " ," + value.toString());
////                    rate.setText(value.get(0));
//                     try{
//                         revenue = Long.valueOf(value.get(3));
//                         aspot = Integer.parseInt((value.get(1)));
//                     } catch (NumberFormatException e) {
//                         e.printStackTrace();
//                     }
//                    Log.i("RATE AND TEMP ", value.get(1));
                } else {
//                    Toast.makeText(MapsActivity.this, "DOCUMENT DOES NOT EXITS", Toast.LENGTH_SHORT).show();
                }
            }
        });



        try{
            userunique = user.getPhoneNumber();
            Log.i("Phone",userunique);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Phone","userunique");
        }

        try{
            userunique = user.getEmail();
            Log.i("Email",userunique);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fstore.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("data", document.getId() + " => " + document.getData());
                                String x = document.getString("Email");
//                                Log.d("TAG", x+" "+userunique);
                                if(x.equals(userunique)){
                                    userunique = document.getString("Phone");
                                    Log.d("TAG", userunique);
                                    break;
                                }
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

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

        if(hour < 12){
            if(timepick1 == true && timepick2 == false) {
                starttime.setText(hrs + ":" + min );
                stime = hrs + ":" + min;
                timepick1 = false;
            }else{
                stoptime.setText(hrs + ":" + min );
                ltime = hrs + ":" + min ;
                timepick2 = false;
            }

        }else{
            stime = hrs + ":" + min;
            if(timepick1 == true && timepick2 == false) {
                starttime.setText(hrs + ":" + min);
                stime = hrs + " : " + min;
                timepick1 = false;
            }else{
                stoptime.setText(hrs + ":" + min);
                ltime = hrs + ":" + min;
                timepick2 = false;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(this,parent.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}