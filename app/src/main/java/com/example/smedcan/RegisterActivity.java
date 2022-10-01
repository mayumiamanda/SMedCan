package com.example.smedcan;
//dilanka

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity {

    Button btn;
    TextView signback;
    EditText name, tel, nic, email, pw, repw;
    FirebaseAuth auth;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn = findViewById(R.id.button);
        signback = findViewById(R.id.signback);
        name = findViewById(R.id.name);
        tel = findViewById(R.id.tel);
        nic = findViewById(R.id.nic);
        email = findViewById(R.id.email);
        pw = findViewById(R.id.pw);
        repw = findViewById(R.id.repw);
        auth = FirebaseAuth.getInstance();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_name = name.getText().toString();
                String str_tel = tel.getText().toString();
                String str_nic = nic.getText().toString();
                String str_email = email.getText().toString();
                String str_pw = pw.getText().toString();
                String str_repw = repw.getText().toString();
                List<String> errorList = new ArrayList<String>();
                if (str_name.isEmpty() || str_email.isEmpty() || str_nic.isEmpty() || str_tel.isEmpty() || str_pw.isEmpty() || str_repw.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All fields required", Toast.LENGTH_SHORT).show();
                } else if (!validateEmail(str_email)) {
                    Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_LONG ).show();
                }else if (!isValidMobile(str_tel)) {
                    Toast.makeText(getApplicationContext(), "Invalid Mobile number", Toast.LENGTH_LONG ).show();
                }else if (!validatenic(str_nic)) {
                    Toast.makeText(getApplicationContext(), "Invalid NIC", Toast.LENGTH_LONG ).show();
                }else if (!isValid(str_pw, str_repw, errorList)) {
                    for (String error : errorList) {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG ).show();
                        break;
                    }

                } else {
                    register(str_name, str_tel, str_nic, str_email, str_pw);
                }
            }
        });

        signback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void register(String name, String tel, String nic, String email, String pw) {
        auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            System.out.println("Email sent");
                        }
                    });
                    String url = Conf.serverurl + "/patientregister";
                    RequestParams params = new RequestParams();
                    params.put("uname", name);
                    params.put("email", email);
                    params.put("nic", nic);
                    params.put("tel", tel);
                    params.put("pw", pw);
                    new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_LONG).show();
                }
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

    public boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public boolean isValidMobile(String s) {
        if (s.length()>10 || s.length()<10){
            return false;
        }else{
            return true;
        }
    }

    public boolean validatenic(String nicStr) {
        if (nicStr.length() == 10 && // only 9 digits
                (nicStr.endsWith("X") || nicStr.endsWith("V"))){
            return true;
        }else{
            return false;
        }
    }
}