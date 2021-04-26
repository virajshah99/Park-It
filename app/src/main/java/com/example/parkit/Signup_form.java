package com.example.parkit;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;

public class Signup_form extends AppCompatActivity {
    EditText txtEmail, txtPassword, txtConfirmPassword,uname;
    String number;
    Button btn_register;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    CountryCodePicker countryCodePicker;
    FirebaseFirestore fStore;
    String userID;
    FirebaseUser user1;

    public void Register(View v) {
        txtEmail = (EditText)findViewById(R.id.txt_email);
        txtPassword = (EditText)findViewById(R.id.password);
        txtConfirmPassword = (EditText)findViewById(R.id.cpassword);
        //countryCodePicker = findViewById(R.id.cpp);
        //progressBar = (ProgressBar) findViewById(R.id.pbar);
        uname = (EditText)findViewById(R.id.name);
        number = getIntent().getStringExtra("number");

        firebaseAuth = FirebaseAuth.getInstance();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        String confirmpassword = txtConfirmPassword.getText().toString().trim();
        final String username = uname.getText().toString().trim();
        //String unumber = number.getText().toString().trim();
         fStore = FirebaseFirestore.getInstance();
        if (TextUtils.isEmpty(email)) {

            Toast.makeText(Signup_form.this, "Please Enter E-mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {

            Toast.makeText(Signup_form.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
            return;

        }
        if (TextUtils.isEmpty(confirmpassword)) {

            Toast.makeText(Signup_form.this, "Please Confirm Your Password", Toast.LENGTH_SHORT).show();
            return;

        }
        if (TextUtils.isEmpty(username)) {

            Toast.makeText(Signup_form.this, "Please Enter Your Name", Toast.LENGTH_SHORT).show();
            return;

        }
        if (password.length() < 8) {
            Toast.makeText(Signup_form.this, "Password Length Too Short ", Toast.LENGTH_SHORT).show();
            return;
        }

        //progressBar.setVisibility(View.VISIBLE);
        if (password.equals(confirmpassword)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Signup_form.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                //Toast.makeText(Signup_form.this, "User Created", Toast.LENGTH_SHORT).show();

                                userID = firebaseAuth.getCurrentUser().getUid();
                                //String phoneno = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                                user1 = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                user1.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("Name Successfully added", "Name Successfully Stored");
                                                }
                                            }
                                        });
                                DocumentReference documentReference = fStore.collection("Users").document(number);
                                Map<String,Object> user = new HashMap<>();
                                user.put("Name",username);
                                user.put("Email",email);
                                user.put("Phone",number);
                                user.put("Password",password);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Signup_form.this, "User Profile Created", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                startActivity(new Intent(getApplicationContext(),Login_form.class));


                            } else {

                                Toast.makeText(Signup_form.this, "Authentication Failed", Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);
        getSupportActionBar().hide();








    }
}
