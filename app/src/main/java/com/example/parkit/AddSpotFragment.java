package com.example.parkit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class AddSpotFragment extends Fragment{ //implements View.OnClickListener {

    static String address="",startTime = "", endTime = "";
    static int rate;



    public AddSpotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("address SpotVC",address);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.i("address",String.valueOf(getActivity()));
        Log.i("address Spot",address);

        View layout = inflater.inflate(R.layout.fragment_add_spot, container, false);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        String addr = sharedPreferences.getString("rentAddress", "");
        String sTime = sharedPreferences.getString("rentSTime", "");
        String eTime = sharedPreferences.getString("rentETime", "");
        int cst = sharedPreferences.getInt("rentCost", -1);

        if (!sTime.equals("") && !eTime.equals("") && !addr.equals("") && cst != -1){
            ImageView imageView = layout.findViewById(R.id.addSpot);
            imageView.setVisibility(View.GONE);
            LinearLayout linearLayout1 = layout.findViewById(R.id.rentDetail);
            linearLayout1.setVisibility(View.VISIBLE);
            Log.i("asda","Changing from addspot");


            EditText address = (EditText) layout.findViewById(R.id.address);
            EditText st = (EditText) layout.findViewById(R.id.stime);
            EditText et = (EditText) layout.findViewById(R.id.etime);
            EditText cost = (EditText) layout.findViewById(R.id.cost);

            address.setText(addr);
            st.setText(sTime);
            et.setText(eTime);
            cost.setText(String.valueOf(cst));
        }else{
            ImageView imageView = layout.findViewById(R.id.addSpot);
            imageView.setVisibility(View.VISIBLE);
            LinearLayout linearLayout1 = layout.findViewById(R.id.rentDetail);
            linearLayout1.setVisibility(View.GONE);
        }

        return layout;
    }

    /*@Override
    public void onClick(View v) {
        Log.i("asda","asda");
        if (v.getId() == R.id.addSpot) {

        }
    }*/
}