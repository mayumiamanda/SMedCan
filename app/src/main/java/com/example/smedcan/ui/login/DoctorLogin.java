package com.example.smedcan.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.Doctor_Home;
import com.example.smedcan.Home;
import com.example.smedcan.R;
import com.example.smedcan.forget_password;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class DoctorLogin extends AppCompatActivity {

    EditText username,password;
    Button login;
    CheckBox togglepw;
    TextView forgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);
        username=findViewById(R.id.user_name);
        password=findViewById(R.id.p_word);
        login=findViewById(R.id.login);
        togglepw=findViewById(R.id.checkBox);
        forgot=findViewById(R.id.forgot);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_uname=username.getText().toString();
                String str_pw=password.getText().toString();
                if (str_uname.isEmpty() || str_pw.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Fields are empty", Toast.LENGTH_SHORT).show();
                }else{
                    doctorLogin(str_uname,str_pw);
                }
            }
        });

        togglepw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int type;
                if (togglepw.isChecked()){
                    type= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
                }else{
                    type=InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }
                password.setInputType(type);
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
    }

    private void doctorLogin(String str_uname, String str_pw) {
        String url = Conf.serverurl + "/doctorlogin";
        RequestParams params = new RequestParams();
        params.put("uname", str_uname);
        params.put("pw", str_pw);
        new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp=new String(responseBody);
                if (!resp.equals("invalid")) {
                    Intent intent=new Intent(getApplicationContext(), Doctor_Home.class);
//                            intent.putExtra("uname",usernameEditText.getText().toString());
                    SharedPreferences sp= getSharedPreferences("smedcan",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("doctor",resp);
                    editor.apply();
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Welcome  ! "+str_uname, Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid credintials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}