package com.example.parkit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class profile extends AppCompatActivity {

    String name,email,phoneNo,password;
    DatabaseReference reference;
    TextInputEditText inputName, inputEmail, inputPhoneNo, inputPassword;
    SharedPreferences sharedPreferences;


    public void updateUser(View v){

        Log.i("Clicked","********************");

        Log.i("name", name);
        Log.i("email", email);
        Log.i("password", password);
        Log.i("name", String.valueOf(name.equals(inputName.getText().toString())));
        Log.i("name", String.valueOf(email.equals(inputEmail.getText().toString())));
        Log.i("name", String.valueOf(password.equals(inputPassword.getText().toString())));
        Log.i("password", inputPassword.getText().toString());
        Log.i("name", inputName.getText().toString());
        Log.i("email", inputEmail.getText().toString());

        if(!name.equals(inputName.getText().toString())){
            reference.child(phoneNo).child("name").setValue(inputName.getText().toString());
            name = inputName.getText().toString();
            Log.i("name", name);
            sharedPreferences.edit().putString("name",name).apply();
        }
        if(!email.equals(inputEmail.getText().toString())){
            reference.child(phoneNo).child("email").setValue(inputEmail.getText().toString());
            email = inputEmail.getText().toString();
            Log.i("email", email);
            sharedPreferences.edit().putString("email",email).apply();
        }
        if(!password.equals(inputPassword.getText().toString())){
            reference.child(phoneNo).child("password").setValue(inputPassword.getText().toString());
            password = inputPassword.getText().toString();
            Log.i("password", password);
            sharedPreferences.edit().putString("password",password).apply();
        }

        /*if(!name.equals(inputName.getText().toString()) || !password.equals(inputPassword.getText().toString()) || !email.equals(inputEmail.getText().toString())){

            new AlertDialog.Builder(getApplicationContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Do you want to save changes?")
                    //.setMessage("Do u want to do this?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(!name.equals(inputName.getText().toString())){
                                reference.child(phoneNo).child("name").setValue(inputName.getText().toString());
                                name = inputName.getText().toString();
                                sharedPreferences.edit().putString("name",name).apply();
                            }
                            if(!email.equals(inputEmail.getText().toString())){
                                reference.child(phoneNo).child("email").setValue(inputEmail.getText().toString());
                                email = inputEmail.getText().toString();
                                sharedPreferences.edit().putString("email",email).apply();
                            }
                            if(!password.equals(inputPassword.getText().toString())){
                                reference.child(phoneNo).child("password").setValue(inputPassword.getText().toString());
                                password = inputPassword.getText().toString();
                                sharedPreferences.edit().putString("password",password).apply();
                            }
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            inputName.setText(name);
                            inputEmail.setText(email);
                            inputPassword.setText(password);
                        }
                    })
                    .show();
        }*/
    }

    public void back(View v){

        if(!name.equals(inputName.getText().toString()) || !password.equals(inputPassword.getText().toString()) || !email.equals(inputEmail.getText().toString())){
            new AlertDialog.Builder(profile.this)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Are you sure you want to leave?")
                    .setMessage("The changes wont be saved.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }else{
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        boolean checkName = name.equals(inputName.getText().toString()),checkPass = password.equals(inputPassword.getText().toString()), checkEmail = email.equals(inputEmail.getText().toString());
        boolean check = !checkEmail || !checkName || !checkPass;
/*
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }*/
        if(check){
            new AlertDialog.Builder(profile.this)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Are you sure you want to leave?")
                    .setMessage("The changes wont be saved.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(profile.this, MapsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }else{
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            finish();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        reference = FirebaseDatabase.getInstance().getReference("user");

        sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        name = sharedPreferences.getString("name","");
        email = sharedPreferences.getString("email","");
        phoneNo = sharedPreferences.getString("phoneNo","");
        password = sharedPreferences.getString("password","");

        inputName = findViewById(R.id.input_Name);
        inputEmail = findViewById(R.id.input_Email);
        inputPhoneNo = findViewById(R.id.input_phoneNo);
        inputPassword = findViewById(R.id.input_Password);

        inputName.setText(name);
        inputEmail.setText(email);
        inputPhoneNo.setText(phoneNo);
        inputPassword.setText(password);

    }
}
