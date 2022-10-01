package com.example.smedcan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.smedcan.Services.CameraService;

import java.net.URI;

public class testActivity extends AppCompatActivity {

    final static int APP_PERMISSION_REQUEST = 158;
    Button b;
    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        }
        b = findViewById(R.id.button3);
        requestPermission();
        System.out.println("okkkkkk");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i <= 10; i++) {
                        System.out.println("Attemptingggggggggggg " + i);
                        Intent front_translucent = new Intent(getApplication()
                                .getApplicationContext(), CameraService.class);
                        front_translucent.putExtra("Front_Request", true);
                        front_translucent.putExtra("Quality_Mode",
                                70);
                        getApplication().getApplicationContext().startService(
                                front_translucent);
                        Thread.sleep(30000);
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(testActivity.this, "button clicked", Toast.LENGTH_SHORT).show();
//                Intent front_translucent = new Intent(getApplication()
//                        .getApplicationContext(), CameraService.class);
//                front_translucent.putExtra("Front_Request", true);
//                front_translucent.putExtra("Quality_Mode",
//                        70);
//                getApplication().getApplicationContext().startService(
//                        front_translucent);
            }
        });
    }

    private void requestPermission() {
//        Toast.makeText(this, "Permission requested", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Toast.makeText(this, "permission checking", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "REQUEST_WRITE_PERMISSION_granted", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "REQUEST_Read_PERMISSION_granted", Toast.LENGTH_SHORT).show();
        }
    }
}