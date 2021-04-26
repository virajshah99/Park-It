package com.example.parkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class qr_code extends AppCompatActivity {

    ImageView imageView;
    String add0,add1,parkinglocname,stime,ltime,carnumberplate;

    public void bckbtn(View v){
        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_qr_code);
        add0 = getIntent().getStringExtra("add0");
        add1 = getIntent().getStringExtra("add1");
        parkinglocname = getIntent().getStringExtra("parkinglocname");
        stime = getIntent().getStringExtra("stime");
        ltime = getIntent().getStringExtra("ltime");
        carnumberplate = getIntent().getStringExtra("Carno");
        TextView locname = findViewById(R.id.textView10);
        TextView Starttime = findViewById(R.id.textView12);
        TextView Stoptime = findViewById(R.id.textView14);
        locname.setText(parkinglocname);
        Starttime.setText("Start Time \n" + stime);
        Stoptime.setText("Leave Time\n"+ ltime);
        imageView = findViewById(R.id.imageView);
        Toast.makeText(qr_code.this, carnumberplate,Toast.LENGTH_SHORT).show();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(carnumberplate, BarcodeFormat.QR_CODE, 200, 200);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            for (int x = 0; x<200; x++){
                for (int y=0; y<200; y++){
                    bitmap.setPixel(x,y,bitMatrix.get(x,y)? Color.BLACK : Color.WHITE);
                }
            }
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDirection(android.view.View view) {



        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + add0 + ",+" + add1 +"&avoid=tf");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
