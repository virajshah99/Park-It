
package com.example.parkit;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.tuyenmonkey.mkloader.MKLoader;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private CountryCodePicker countryCodePicker;
    private EditText number;
    private Button next;
    private Button login;
    public void loginredirect(View view){
        Intent intent = new Intent(PhoneLoginActivity.this,Login_form.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        getSupportActionBar().hide();
        countryCodePicker = findViewById(R.id.ccp);
        number = findViewById(R.id.editText_carrierNumber);
        next = findViewById(R.id.next);
        countryCodePicker.registerCarrierNumberEditText(number);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // User is signed in
            Intent i = new Intent(PhoneLoginActivity.this, MapsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(number.getText().toString())){
                    Toast.makeText(PhoneLoginActivity.this, "Enter No ....", Toast.LENGTH_SHORT).show();
                }
                else if(number.getText().toString().replace(" ","").length()!=10){
                    Toast.makeText(PhoneLoginActivity.this, "Enter Correct No ...", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(PhoneLoginActivity.this,VerificationActivity.class);
                    intent.putExtra("number",countryCodePicker.getFullNumberWithPlus().replace(" ",""));
                    startActivity(intent);
                    finish();
                }
            }
        });



    }




}
