package com.example.smedcan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.data.model.Doctor;
import com.example.smedcan.data.model.Patients;
import com.example.smedcan.ui.login.Splas;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Doctor_profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Doctor_profile extends Fragment {

    AppCompatButton goOnline,logout;
    AppCompatTextView name,nic,email,mobile,online_time,bio;
//    AppCompatEditText online_time,bio;
    private final String[] permissions = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    private final int requestcode = 1;
    StorageReference storageReference;

    private Uri mImageUri;
    private StorageTask uplaodTask;

    public static CircleImageView img;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Doctor_profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Doctor_profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Doctor_profile newInstance(String param1, String param2) {
        Doctor_profile fragment = new Doctor_profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doctor_profile, container, false);
        goOnline=view.findViewById(R.id.go_online);
        logout=view.findViewById(R.id.Log_out);
        name=view.findViewById(R.id.name);
        online_time=view.findViewById(R.id.online_time);
        nic=view.findViewById(R.id.Nic);
        email=view.findViewById(R.id.email);
        mobile=view.findViewById(R.id.Mobile);
        bio=view.findViewById(R.id.bio);
        img=view.findViewById(R.id.doc_image);
        storageReference = FirebaseStorage.getInstance().getReference("Doctors");

        Gson gson=new Gson();
        String url = Conf.serverurl + "/loaddocprofile";
        RequestParams params = new RequestParams();
        params.put("did", Doctor_Home.uname);
        new AsyncHttpClient().get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response=new String(responseBody);
                Doctor d = gson.fromJson(response, Doctor.class);

                name.setText(d.getDname());
                online_time.setText(d.getOtperiod());
                nic.setText(d.getNic());
                email.setText(d.getEmail());
                mobile.setText(String.valueOf(d.getCoNumber()));
                bio.setText(d.getEdQualification()+" , "+d.getAqualification());
                if (d.getImgurl()!=null){
                    Picasso.get().load(d.getImgurl()).into(img);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });


        goOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPermissionGranted()) {
                    askPermissions();
                    Intent intent = new Intent(getActivity(), CallActivity.class);
                    intent.putExtra("did", Doctor_Home.uname);
                    intent.putExtra("user", "doctor");
                    getActivity().startActivity(intent);
                }else{
                    Intent intent = new Intent(getActivity(), CallActivity.class);
                    intent.putExtra("did", Doctor_Home.uname);
                    intent.putExtra("user", "doctor");
                    getActivity().startActivity(intent);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp= getActivity().getSharedPreferences("smedcan", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                Intent intent=new Intent(getActivity(), Splas.class);
                getActivity().startActivity(intent);
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(getActivity());
            }
        });

        return view;
    }

    private final void askPermissions() {
        ActivityCompat.requestPermissions(getActivity(), this.permissions, this.requestcode);
    }

    private final boolean isPermissionGranted() {
        boolean grant=true;
        for (String s: permissions) {
            int res =getActivity().checkCallingOrSelfPermission(s);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                grant=false;
            }
        }

        return  grant;
    }


}