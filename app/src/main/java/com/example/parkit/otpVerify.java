package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class otpVerify extends AppCompatActivity {

    ProgressBar progressBar;
    String verificationCodeBySystem,phoneNo,name,email,password;
    Button confirm;
    EditText verificationCode;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    FirebaseAuth firebaseAuth;

    public void changeNumber(View view){
        Intent intent = new Intent(getApplicationContext(), SignUp.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        confirm = findViewById(R.id.verify);
        progressBar = findViewById(R.id.progressBar);
        verificationCode = findViewById(R.id.editText);

        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        phoneNo = getIntent().getStringExtra("phoneNo");
        password = getIntent().getStringExtra("password");

        Button checkNumber = findViewById(R.id.checkNumber);
        checkNumber.setText(phoneNo+" ?");
        sendVerificationCodeToUser(phoneNo);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = verificationCode.getText().toString();

                if (code.isEmpty() || code.length() < 6) {
                    verificationCode.setError("Wrong OTP...");
                    verificationCode.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });

    }

    private void sendVerificationCodeToUser(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNo,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,   // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(otpVerify.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);
    }

    private void signInTheUserByCredentials(PhoneAuthCredential credential) {

        firebaseAuth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("user");

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(otpVerify.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    ///creating user in realtime database
                    Log.i("name" , name);
                    Log.i("phone" , phoneNo);
                    Log.i("email" , email);
                    Log.i("pass" , password);
                    UserHelperClass helperClass = new UserHelperClass(email, name, phoneNo, password);
                    reference.child(phoneNo).setValue(helperClass);

                    Toast.makeText(otpVerify.this, "Your Account has been created successfully!", Toast.LENGTH_SHORT).show();

                    //Perform Your required action here to either let the user sign In or do something required
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(otpVerify.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
