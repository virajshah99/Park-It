package com.example.parkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class FreeParkSpot extends AppCompatActivity {

    String link;

    public void freespace(View view){
        Uri gmmIntentUri = Uri.parse(link);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_park_spot);
        link = getIntent().getStringExtra("link");
    }
}