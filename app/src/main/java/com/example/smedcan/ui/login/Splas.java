package com.example.smedcan.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smedcan.Doctor_Home;
import com.example.smedcan.Home;
import com.example.smedcan.MainActivity;
import com.example.smedcan.R;

public class Splas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splas);
        SharedPreferences sp= getSharedPreferences("smedcan",MODE_PRIVATE);
        String uname = sp.getString("user", "Not Found");
        String doc = sp.getString("doctor", "Not Found");
        if (!uname.equals("Not Found")){
            Intent intent=new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        }else if (!doc.equals("Not Found")){
            Intent intent=new Intent(getApplicationContext(), Doctor_Home.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}