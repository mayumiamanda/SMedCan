package com.example.smedcan.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.Home;
import com.example.smedcan.R;
import com.example.smedcan.data.model.Doctor;
import com.example.smedcan.data.model.Patients;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorProfileDialog extends DialogFragment {

    AlertDialog alertDialog;
    AppCompatTextView name,gender,email,contact,avtime,qualification;
    AppCompatButton close;
    CircleImageView img;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.doctor_profile, null);
        name=view.findViewById(R.id.name);
        gender=view.findViewById(R.id.gender);
        email=view.findViewById(R.id.email);
        contact=view.findViewById(R.id.Mobile);
        avtime=view.findViewById(R.id.online_time);
        qualification=view.findViewById(R.id.Education);
        close=view.findViewById(R.id.close);
        img=view.findViewById(R.id.user_image);

        Bundle bundle = getArguments();
        String did = bundle.getString("did");
        alert.setView(view);

        alertDialog = alert.create();

        Gson gson=new Gson();
        String url = Conf.serverurl + "/loaddocprofile";
        RequestParams params = new RequestParams();
        params.put("did", did);
        new AsyncHttpClient().get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response=new String(responseBody);
                Doctor doc = gson.fromJson(response, Doctor.class);
                name.setText(doc.getDname());
                gender.setText(doc.getGender());
                email.setText(doc.getEmail());
                contact.setText("Contact no : "+String.valueOf(doc.getCoNumber()));
                avtime.setText(doc.getOtperiod());
                qualification.setText(doc.getEdQualification() +" , "+doc.getAqualification());
                if (doc.getImgurl()!=null && !doc.getImgurl().equals("")){
                    Picasso.get().load(doc.getImgurl()).into(img);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        return alertDialog;
    }
}
