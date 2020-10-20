package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    TextInputLayout loginPhoneNo, loginPassword;
    ProgressBar progressBar;
    String num,name1,email1,password1;

    public void login(View view){
        //Validate Login Info
        if (!validatePhoneNo() | !validatePassword()) {
            return;
        }else {
            isUser();
        }
    }

    public void register(View view){
        Intent intent = new Intent(getApplicationContext(),SignUp.class);
        startActivity(intent);
        finish();
    }

    private Boolean validatePhoneNo() {
        String val = loginPhoneNo.getEditText().getText().toString();

        if (val.isEmpty()) {
            loginPhoneNo.setError("Field cannot be empty");
            return false;
        } else {
            loginPhoneNo.setError(null);
            loginPhoneNo.setErrorEnabled(false);
            return true;
        }
    }
    private Boolean validatePassword() {
        String val = loginPassword.getEditText().getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Field cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            loginPassword.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPhoneNo = findViewById(R.id.loginPhoneNo);
        loginPassword = findViewById(R.id.loginPassword);
        progressBar = findViewById(R.id.progressbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
//            String phoneNo = user.getPhoneNumber();
//            num = phoneNo.replace("+91","").trim();
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
//            Log.i("phoneNo", num);
//            //Query checkUser = reference.orderByChild("phoneNo").equalTo(num);
//            reference.orderByChild("phoneNo").equalTo(num).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.i("asdasda","Entered");
//                    name1 = dataSnapshot.child(num).child("name").getValue(String.class);
//                    email1 = dataSnapshot.child(num).child("email").getValue(String.class);
//                    password1 = dataSnapshot.child(num).child("password").getValue(String.class);
//
//                    if(name1 != null){
//                        Log.i("name", name1);
//                        Log.i("email", email1);
//                        Log.i("password", password1);
//                        Intent i = new Intent(Login.this, MapsActivity.class);
//                        i.putExtra("name", name1);
//                        i.putExtra("email", email1);
//                        i.putExtra("phoneNo", num);
//                        i.putExtra("password", password1);
//                        startActivity(i);
//                    }
//                }
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                }
//            });
            Log.i("user",user.getEmail());
            Intent i = new Intent(Login.this, MapsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }else{
            Log.i("asdas","asdazx");
        }
    }

    private void isUser() {
        progressBar.setVisibility(View.VISIBLE);
        final String userEnteredPhoneNo = "+91" +  loginPhoneNo.getEditText().getText().toString().trim();
        final String userEnteredPassword = loginPassword.getEditText().getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        Query checkUser = reference.orderByChild("phoneNo").equalTo(userEnteredPhoneNo);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    loginPhoneNo.setError(null);
                    loginPhoneNo.setErrorEnabled(false);
                    String passwordFromDB = dataSnapshot.child(userEnteredPhoneNo).child("password").getValue(String.class);
                    if(passwordFromDB.equals(userEnteredPassword)){
                        loginPhoneNo.setError(null);
                        loginPhoneNo.setErrorEnabled(false);
                        //sending data from db to maps activity
                        String nameFromDB = dataSnapshot.child(userEnteredPhoneNo).child("name").getValue(String.class);
                        String phoneNoFromDB = dataSnapshot.child(userEnteredPhoneNo).child("phoneNo").getValue(String.class);
                        String emailFromDB = dataSnapshot.child(userEnteredPhoneNo).child("email").getValue(String.class);

                        SharedPreferences sharedPreferences = Login.this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("name",nameFromDB).apply();
                        sharedPreferences.edit().putString("email",emailFromDB).apply();
                        sharedPreferences.edit().putString("phoneNo",phoneNoFromDB).apply();
                        sharedPreferences.edit().putString("password",passwordFromDB).apply();

                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
//                        intent.putExtra("name", nameFromDB);
//                        intent.putExtra("email", emailFromDB);
//                        intent.putExtra("phoneNo", phoneNoFromDB);
//                        intent.putExtra("password", passwordFromDB);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        loginPassword.setError("Wrong Password");
                        loginPassword.requestFocus();
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    loginPhoneNo.setError("No such User exist");
                    loginPhoneNo.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
