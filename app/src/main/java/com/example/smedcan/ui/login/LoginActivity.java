package com.example.smedcan.ui.login;
//dilanka

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.Home;
import com.example.smedcan.Profile;
import com.example.smedcan.R;
import com.example.smedcan.RegisterActivity;
import com.example.smedcan.data.Result;
import com.example.smedcan.data.model.LoggedInUser;
import com.example.smedcan.data.model.Patients;
import com.example.smedcan.forget_password;
import com.example.smedcan.ui.login.LoginViewModel;
import com.example.smedcan.ui.login.LoginViewModelFactory;
import com.example.smedcan.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    FirebaseUser firebaseUser;
    FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final TextView tosignup = binding.textView4;
        final TextView forgot = binding.forgot;
        final Button loginButton = binding.login;
        final CheckBox togglepw = binding.checkBox;
        final ProgressBar loadingProgressBar = binding.loading;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                Gson gson=new Gson();
//                loginViewModel.login(usernameEditText.getText().toString(),
//                        passwordEditText.getText().toString());
                String url = Conf.serverurl + "/patientlogin";
                RequestParams params = new RequestParams();
                params.put("uname", usernameEditText.getText().toString());
                params.put("pw", passwordEditText.getText().toString());
                new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String resp=new String(responseBody);
                        if (!resp.equals("invalid")) {
                            Patients p = gson.fromJson(resp, Patients.class);
                            auth.signInWithEmailAndPassword(p.getEmail(),p.getPassword()).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        firebaseUser = auth.getCurrentUser();

                                        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                            Intent intent=new Intent(getApplicationContext(), Home.class);
//                            intent.putExtra("uname",usernameEditText.getText().toString());
                                            SharedPreferences sp= getSharedPreferences("smedcan",MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();

                                            editor.putString("user",usernameEditText.getText().toString());
                                            editor.apply();
                                            startActivity(intent);
                                            Toast.makeText(getApplicationContext(), "Welcome  ! "+usernameEditText.getText().toString(), Toast.LENGTH_LONG).show();
                                            finish();
                                        } else {
                                            loadingProgressBar.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this, "Please verify your Email.", Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        loadingProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else{
                            loadingProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Invalid credintials", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), forget_password.class);
                startActivity(intent);
                finish();
            }
        });

        togglepw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int type;
                if (togglepw.isChecked()){
                    type=InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
                }else{
                    type=InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }
                passwordEditText.setInputType(type);
            }
        });
    }
}