package com.example.smedcan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.data.model.Patients;
import com.example.smedcan.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class forget_password extends AppCompatActivity {
    ProgressBar progress;
    AppCompatEditText pw, repw, otp, email;
    LinearLayoutCompat otplayout, resetlayout;
    AppCompatButton sendemail, resetpw;
    TextView back;
    String str_email;
    String old_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        progress = findViewById(R.id.progress);
        pw = findViewById(R.id.pw1);
        repw = findViewById(R.id.pw2);
        otp = findViewById(R.id.otp);
        email = findViewById(R.id.email);
        sendemail = findViewById(R.id.sendemail);
        resetpw = findViewById(R.id.resetpw);
        back = findViewById(R.id.back);
        otplayout = findViewById(R.id.linearLayoutCompat1);
        resetlayout = findViewById(R.id.linearLayoutCompat);

        sendemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    str_email = email.getText().toString();
                    if (!str_email.isEmpty()) {
                        progress.setVisibility(View.VISIBLE);
                        String url = Conf.serverurl + "/sendemail";
                        RequestParams params = new RequestParams();
                        params.put("email", str_email);
                        new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String resp = new String(responseBody);
                                if (!resp.equals("failed")) {
                                    Gson gson=new Gson();
                                    Patients p = gson.fromJson(resp, Patients.class);
                                    old_pw=p.getPassword();
                                    progress.setVisibility(View.GONE);
                                    otplayout.setVisibility(View.GONE);
                                    resetlayout.setVisibility(View.VISIBLE);
                                } else {
                                    progress.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Invalid Email Address", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Email field is empty", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    progress.setVisibility(View.GONE);
                }
            }
        });

        resetpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strpw = pw.getText().toString();
                String strrepw = repw.getText().toString();
                String strotp = otp.getText().toString();
                List<String> errorList = new ArrayList<String>();
                if (strpw.isEmpty() || strrepw.isEmpty() || strotp.isEmpty() || str_email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Some fields are empty", Toast.LENGTH_SHORT).show();
                }else if (!isValid(strpw, strrepw, errorList)) {
                    for (String error : errorList) {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG ).show();
                        break;
                    }

                } else {
                    resetPassword(str_email, strpw, strotp);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void resetPassword(String str_email, String strpw, String strotp) {
        progress.setVisibility(View.VISIBLE);
        String url = Conf.serverurl + "/resetpw";
        RequestParams params = new RequestParams();
        params.put("email", str_email);
        params.put("pw", strpw);
        params.put("otp", strotp);
        new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp = new String(responseBody);
                if (!resp.equals("failed")) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(str_email, old_pw);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseUser.updatePassword(strpw).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progress.setVisibility(View.GONE);
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "re-authenticate issue", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else {
                                Toast.makeText(getApplicationContext(), "Failed to re-authenticate", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } else {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isValid(String passwordhere, String confirmhere, List<String> errorList) {

        Pattern specailCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Pattern digitCasePatten = Pattern.compile("[0-9 ]");
        errorList.clear();

        boolean flag=true;

        if (!passwordhere.equals(confirmhere)) {
            errorList.add("password and confirm password does not match");
            flag=false;
        }
        if (passwordhere.length() < 8) {
            errorList.add("Password lenght must have alleast 8 character !!");
            flag=false;
        }
        if (!specailCharPatten.matcher(passwordhere).find()) {
            errorList.add("Password must have atleast one specail character !!");
            flag=false;
        }
        if (!UpperCasePatten.matcher(passwordhere).find()) {
            errorList.add("Password must have atleast one uppercase character !!");
            flag=false;
        }
        if (!lowerCasePatten.matcher(passwordhere).find()) {
            errorList.add("Password must have atleast one lowercase character !!");
            flag=false;
        }
        if (!digitCasePatten.matcher(passwordhere).find()) {
            errorList.add("Password must have atleast one digit character !!");
            flag=false;
        }

        return flag;

    }
}