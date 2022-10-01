package com.example.smedcan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import cz.msebera.android.httpclient.Header;

public class Doctor_Home extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    public static String uname;
    StorageReference storageReference;

    private Uri mImageUri;
    private StorageTask uplaodTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);
        SharedPreferences sp= getSharedPreferences("smedcan",MODE_PRIVATE);
        storageReference = FirebaseStorage.getInstance().getReference("Doctors");
        uname = sp.getString("doctor", "Not Found");
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        selectedFragment = new History_List();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()) {

                case R.id.nav_call_history:
                    selectedFragment = new History_List();
                    break;

                case R.id.nav_dprofile:
                    selectedFragment = new Doctor_profile();
                    break;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        }


    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                mImageUri = result.getUri();
            }

            uploadImage();
        } else {
            Toast.makeText(getApplicationContext(), "Something gone wrong..! ", Toast.LENGTH_SHORT).show();

        }
    }

    private void uploadImage() {

        if (mImageUri != null) {
            final StorageReference filerefrence = storageReference.child(Doctor_Home.uname+ ".jpg");
            uplaodTask = filerefrence.putFile(mImageUri);

            uplaodTask = filerefrence.putFile(mImageUri);

            uplaodTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filerefrence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = null;
                        if (downloadUri != null) {
                            myUrl = downloadUri.toString();
                        }

                        if (myUrl!=null){
                            Picasso.get().load(myUrl).into(Doctor_profile.img);
                            String url = Conf.serverurl + "/updatedoctorimg";
                            RequestParams params = new RequestParams();
                            params.put("imgurl", myUrl);
                            params.put("did", Doctor_Home.uname);
                            new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    Toast.makeText(getApplicationContext().getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Toast.makeText(getApplicationContext().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                                    error.printStackTrace();
                                }
                            });
                        }


                    } else {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            Toast.makeText(getApplicationContext(), "No image selected ", Toast.LENGTH_SHORT).show();


        }
    }
}