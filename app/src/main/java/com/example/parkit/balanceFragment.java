package com.example.parkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class balanceFragment extends Fragment {

    List<String> key,value;
    FirebaseFirestore fstore;
    Long revenue;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    public balanceFragment() {
        // Required empty public constructor
    }

    View layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        layout = inflater.inflate(R.layout.fragment_balance, container, false);
        fstore = FirebaseFirestore.getInstance();


        key = new ArrayList<String>();
        value = new ArrayList<String>();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        String s = sharedPreferences.getString("rentAddress", "");

        if(!s.equals("")){
            TextView t1 = layout.findViewById(R.id.userRevenue);
//            t1.setText("Rs. 80");
//
            DocumentReference fetchdata2 = fstore.collection("Parking Spots").document(s);
            fetchdata2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> datafirestore = documentSnapshot.getData();

                        revenue = documentSnapshot.getLong("Revenue");
//                        String temp;
//                        for (Map.Entry<String, Object> entry : datafirestore.entrySet()) {
//                            String k = entry.getKey();
//                            String v = String.valueOf(entry.getValue());
//                            key.add(k);
//                            value.add(v);
//                        }
//                        Log.i("MESSAGE", key.toString() + " ," + value.toString());
//    //                    rate.setText(value.get(0));
//                        revenue = Long.valueOf(value.get(3));
                        TextView t1 =(TextView) layout.findViewById(R.id.userRevenue);
                        t1.setText("Rs. "+String.valueOf(revenue));
                        Log.i("RATE AND TEMP ", String.valueOf(revenue));
                    } else {
    //                    Toast.makeText(MapsActivity.this, "DOCUMENT DOES NOT EXITS", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            TextView t1 = layout.findViewById(R.id.userRevenue);
            t1.setText("Add your own Parking Spot to generate Revenue");
        }

        return layout;
    }
}