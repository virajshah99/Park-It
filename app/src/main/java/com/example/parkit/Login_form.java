package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login_form extends AppCompatActivity {
    ProgressBar progressBar;
    EditText tmail,tpass;
    Button btn_login;
    private FirebaseAuth firebaseAuth;
    public void btn_signupform(View view)
    {
        startActivity(new Intent(getApplicationContext(),PhoneLoginActivity.class));
    }


    private boolean isNetConnected() {
        ConnectivityManager cm;
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);
        getSupportActionBar().hide();

        isNetConnected();

        tmail = (EditText) findViewById(R.id.e1);
        tpass = (EditText) findViewById(R.id.p1);
        btn_login = (Button) findViewById(R.id.buttonlogin);

        //progressBar.setVisibility(View.GONE);
        firebaseAuth = FirebaseAuth.getInstance();
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = tmail.getText().toString().trim();
                String password = tpass.getText().toString().trim();
           //     progressBar.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(email)) {

                    Toast.makeText(Login_form.this, "Please Enter E-mail", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {

                    Toast.makeText(Login_form.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (password.length() < 8) {
                    Toast.makeText(Login_form.this, "Password Length Too Short ", Toast.LENGTH_SHORT).show();
                }
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login_form.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                                    //SharedPreferences LoginCheck =
                                } else {


                                    Toast.makeText(Login_form.this, "Login Failed / User Not Registered", Toast.LENGTH_SHORT).show();
                                }
                            }


                        });

            }


        });


    }}

