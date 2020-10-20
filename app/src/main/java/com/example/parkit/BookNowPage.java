package com.example.parkit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class BookNowPage extends AppCompatActivity implements TimePickerFragment.TimePickerListener, AdapterView.OnItemSelectedListener {

    private TextView tvDisplayTime;

    public void book(View view){
        Intent intent = new Intent(getApplicationContext(), qr_code.class);
        startActivity(intent);
    }

    public void getTime(View view){
        tvDisplayTime = findViewById(R.id.startTime);
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setCancelable(false);
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_now_page);

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Spinner spinnerTime = (Spinner) findViewById(R.id.spinnerTime);
//        Spinner spinnerCarNo = (Spinner) findViewById(R.id.spinnerCarNo);//TODO: remove comment
        Spinner spinnerFuel = (Spinner) findViewById(R.id.spinnerFuel);

        EditText abc = (EditText) findViewById(R.id.carno);
        abc.setText("MH01AB1234");

        //for cars
        spinnerFuel.setOnItemSelectedListener(this);

        //for time in hours
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.hours,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);
        spinnerTime.setOnItemSelectedListener(this);


        EditText nameTextBox = (EditText) findViewById(R.id.nameTextBox);
        /*SharedPreferences sharedPreferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String tag = sharedPreferences.getString("name","");
        nameTextBox.setText(tag);*/ //TODO: remove comment

        nameTextBox.setText("Viraj Shah");

        //setting data from database
        /*try{
//          String tag = user.getPhoneNumber().toString();

            String tag = user.getDisplayName();
            Log.i("asdasdasdasdsa",tag);
            nameTextBox.setText(tag);

            }catch (Exception e){
                System.out.println(e);
            }
*/
    }
    //required for picking time
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String min =  String.valueOf(minute);
        String hrs =  String.valueOf(hour);
        if(hrs.length() == 1){
            hrs = "0" + hrs;
        }
        if(min.length() == 1){
            min = "0"+ min;
        }
        if(hour < 12){
            tvDisplayTime.setText(hrs + " : " + min +"am");
        }else{
            tvDisplayTime.setText(hrs + " : " + min +"pm");
        }
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this,parent.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

