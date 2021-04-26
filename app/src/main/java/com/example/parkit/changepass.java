package com.example.parkit;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class changepass extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText pno,oldpass,newpass,email;
    DocumentReference changeemail;
    String phoneno,oldp,newp,oldpassword,temp,emailaddress;
    Button confirm;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fStore = FirebaseFirestore.getInstance();
        getSupportActionBar().hide();
        setContentView(R.layout.activity_changepass);
        //oldpass = findViewById(R.id.oldpass);
        //newpass = findViewById(R.id.newpass);
        //pno = findViewById(R.id.pno);
        confirm = findViewById(R.id.confirmpass);
        email = findViewById(R.id.email);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth = FirebaseAuth.getInstance();
                emailaddress = email.getText().toString();
                auth.sendPasswordResetEmail(emailaddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("EMAIL SENT", "Email sent.");
                                    Toast.makeText(getApplicationContext(), "Check Your Email", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(changepass.this,MapsActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(changepass.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


///========================================================================FIRE BASE UPDATE DATABASE WITH PHONE NUMBER =========================================//

        /*confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temp = String.valueOf((pno.getText()));
                phoneno = "+91"+temp;
                oldp = String.valueOf(oldpass.getText());
                newp = String.valueOf(newpass.getText());
                Log.i("PHONE NO ",phoneno);
                changeemail = fStore.collection("Users").document(phoneno);
                changeemail.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){

                             oldpassword = documentSnapshot.getString("Password");
                            if(oldpassword.equals(oldp) ){
                                changeemail.update("Password",newp);
                                Log.i("Password Updated", oldp + "to"+ newp);


                            }
                            else{

                                Toast.makeText(changepass.this, "Please Enter valid Password", Toast.LENGTH_SHORT).show();

                            }
                        }
                        Log.i("NEW PASSWORD",documentSnapshot.getString("Password"));
                        Intent intent = new Intent(changepass.this,MapsActivity.class);
                        startActivity(intent);
                    }
                });


            }
        });*/

    }
}
