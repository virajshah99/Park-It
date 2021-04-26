package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class RentSpot extends AppCompatActivity{
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore fStore;

    PagerAdapter pagerAdapter;
    ViewPager viewPager;

    public void addPSpot(View view){
        Intent intent = new Intent(getApplicationContext(),AddOnMap.class);
        startActivity(intent);
        finish();
    }

    public void deleteRentInfo(View view) {
        new AlertDialog.Builder(RentSpot.this)
                .setIcon(R.drawable.ic_error)
                .setTitle("Are you sure you want to delete?")
                .setMessage("The Rent Parking data will be deleted permanently.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPreferences = RentSpot.this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                        String S = sharedPreferences.getString("rentAddress", "");

                        fStore.collection("Parking Spots").document(S)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Deleted", "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Error", "Error deleting document", e);
                                    }
                                });

                        DatabaseReference ref = firebaseDatabase.getReference();
                        Query applesQuery = ref.child("Parking Spots").child(S);

                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("Deleted", "onCancelled", databaseError.toException());
                            }
                        });


                        sharedPreferences.edit().putString("rentSTime", "").apply();
                        sharedPreferences.edit().putString("rentETime", "").apply();
                        sharedPreferences.edit().putString("rentAddress", "").apply();
                        sharedPreferences.edit().putInt("rentCost", -1).apply();

                        EditText address = (EditText) findViewById(R.id.address);
                        EditText st = (EditText) findViewById(R.id.stime);
                        EditText et = (EditText) findViewById(R.id.etime);
                        EditText cost = (EditText) findViewById(R.id.cost);

                        address.setText("");
                        st.setText("");
                        et.setText("");
                        cost.setText("");

                        LinearLayout linearLayout1 = findViewById(R.id.rentDetail);
                        linearLayout1.setVisibility(View.GONE);
                        ImageView imageView = findViewById(R.id.addSpot);
                        imageView.setVisibility(View.VISIBLE);

                        Intent intent = new Intent(getApplicationContext(),RentSpot.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();

    }

    public void bbtn(View v) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_rent_spot);
        fStore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        TabLayout tabLayout = findViewById(R.id.tabBar);
        //TabItem tabHistory = findViewById(R.id.tabHistory);
        TabItem tabBalance = findViewById(R.id.tabBalance);
        TabItem tabAddSpot = findViewById(R.id.tabAddSpot);
        viewPager = findViewById(R.id.viewPager);


        pagerAdapter = new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    public void onResume() {
        if (viewPager != null && viewPager.getAdapter() != null) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
        super.onResume();
    }

}
