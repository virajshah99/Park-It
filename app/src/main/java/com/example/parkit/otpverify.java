package com.example.parkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class otpverify extends AppCompatActivity {
     private EditText otp;
     private Button verify;
     private TextView resend;
     private String number,id;
     FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverify);
        otp = (EditText) findViewById(R.id.otpid);
        verify = findViewById(R.id.verifybutton);
        resend = findViewById(R.id.resend1);
        firebaseAuth = FirebaseAuth.getInstance();
        number = getIntent().getStringExtra("number");
        Toast.makeText(this, "number"+number, Toast.LENGTH_SHORT).show();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(otp.getText().toString())){

                    Toast.makeText(otpverify.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                }
                else if(otp.getText().toString().replace(" ","").length()!=6){
                    Toast.makeText(otpverify.this, "Enter Right OTP", Toast.LENGTH_SHORT).show();
                }
                else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id,otp.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendverificationcode();
            }
        });

    }

    private void sendverificationcode() {

        new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long l) {
                resend.setText(""+l/1000);
                resend.setEnabled(false);

            }

            @Override
            public void onFinish() {
                resend.setText("Resend");
                resend.setEnabled(true);

            }
        }.start();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        otpverify.this.id=id;
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(otpverify.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });        // OnVerificationStateChangedCallbacks




    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(otpverify.this,Login_form.class));
                            finish();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            Toast.makeText(otpverify.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }





}
