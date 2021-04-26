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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class changeemail extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText pno1,oldemail,newemail;
    DocumentReference changeemail;
    String phoneno,olde,newe,oldeemail,temp,usermail;
    FirebaseUser user;
    Button confirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fStore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_changeemail);
        //pno1 = findViewById(R.id.pno1);
        getSupportActionBar().hide();
        oldemail = findViewById(R.id.oldemail);
        newemail = findViewById(R.id.newemail);
        confirm = findViewById(R.id.confirm);
         user = FirebaseAuth.getInstance().getCurrentUser();
         confirm.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 newe = newemail.getText().toString(); // new text box mail
                 olde = oldemail.getText().toString(); // old text box mail
                 usermail = user.getEmail();
                 if (usermail.equals(olde)) {
                     user.updateEmail(newe)
                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         Log.d("EMAIL ADDRESS UPDATED", "User email address updated.");
                                         Intent intent = new Intent(changeemail.this,MapsActivity.class);
                                         startActivity(intent);
                                     }
                                 }
                             });
                 } else {
                     Toast.makeText(changeemail.this, "Wrong current Email", Toast.LENGTH_SHORT).show();

                 }
             }
         });

         //=================================================================== REAL TIME UPDATE ON DATABASE =====================================================//
        /*confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                temp = String.valueOf((pno1.getText()));
                phoneno = "+91"+temp;
                olde = String.valueOf(oldemail.getText());
                newe = String.valueOf(newemail.getText());
                changeemail = fStore.collection("Users").document(phoneno);
                changeemail.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            oldeemail = documentSnapshot.getString("Email");
                            Log.i("OLD EMAIL",oldeemail);
                            if(oldeemail.equals(olde)){

                                changeemail.update("Email",newe);
                                Log.i("Email Updated", olde + "to"+ newe);


                            }
                            else{

                                Toast.makeText(com.example.parkit.changeemail.this, "Please Enter valid Email Address", Toast.LENGTH_SHORT).show();

                            }
                        }
                        Log.i("NEW EMAIL ID",documentSnapshot.getString("Email"));
                        Intent intent = new Intent(changeemail.this,MapsActivity.class);
                        startActivity(intent);
                    }
                });


            }
        });*/
    }
}
