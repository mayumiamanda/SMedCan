package com.example.smedcan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.data.model.Patients;
import com.example.smedcan.ui.login.Splas;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {
    AppCompatButton edit,logout;
    AppCompatEditText age,bio;
    AppCompatTextView name,nic,email,tel;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    private Uri mImageUri;
    private StorageTask uplaodTask;
    CircleImageView img;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Patients");
        edit=view.findViewById(R.id.edit_profile);
        logout=view.findViewById(R.id.Log_out);
        age=view.findViewById(R.id.Age);
        bio=view.findViewById(R.id.bio);
        name=view.findViewById(R.id.name);
        nic=view.findViewById(R.id.Nic);
        email=view.findViewById(R.id.email);
        tel=view.findViewById(R.id.Mobile);
        img=view.findViewById(R.id.user_image);
        Gson gson=new Gson();
        String url = Conf.serverurl + "/loadpatient";
        RequestParams params = new RequestParams();
        params.put("uname", Home.uname);
        new AsyncHttpClient().get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response=new String(responseBody);
                Patients p = gson.fromJson(response, Patients.class);

                name.setText(p.getUsername());
                nic.setText(p.getNic());
                email.setText(p.getEmail());
                tel.setText(p.getTel());
                if (p.getAge()!=null){
                    age.setText(p.getAge());
                }
                if (p.getDescription()!=null){
                    bio.setText(p.getDescription());
                }
                if (p.getImgurl()!=null){
                    Picasso.get().load(p.getImgurl()).into(img);
                }
//                try {
//                    JSONArray jsonArr = new JSONArray(response);
//                    List<Patients> lstExtrextData = new ArrayList<>();
//                    for (int i = 0; i < jsonArr.length(); i++) {
//                        JSONObject jsonObj = jsonArr.getJSONObject(i);
//                        System.out.println(jsonObj.get("nic"));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Conf.serverurl + "/updatepatient";
                RequestParams params = new RequestParams();
                params.put("age", age.getText().toString());
                params.put("description", bio.getText().toString());
                params.put("uname", Home.uname);
                new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (!age.getText().toString().isEmpty()){
                            age.setFocusable(false);
                            age.setClickable(false);
                        }
                        if (!bio.getText().toString().isEmpty()){
                            bio.setFocusable(false);
                            bio.setClickable(false);
                        }
                        Toast.makeText(getActivity().getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("bbbbbbbbbbbbbbbb");
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result != null) {
                mImageUri = result.getUri();
            }

            uploadImage();
        } else {
            Toast.makeText(getActivity(), "Something gone wrong..! ", Toast.LENGTH_SHORT).show();

        }
    }

    private void uploadImage() {

        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Updating your Image");
        pd.show();
        pd.setCanceledOnTouchOutside(false);


        if (mImageUri != null) {
            final StorageReference filerefrence = storageReference.child(firebaseUser.getUid()+ ".jpg");
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
                            Picasso.get().load(myUrl).into(img);
                            String url = Conf.serverurl + "/updatepatientimg";
                            RequestParams params = new RequestParams();
                            params.put("imgurl", myUrl);
                            params.put("uname", Home.uname);
                            new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();

                                    pd.dismiss();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                                    error.printStackTrace();
                                    pd.dismiss();
                                }
                            });
                        }


                    } else {
                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            Toast.makeText(getActivity(), "No image selected ", Toast.LENGTH_SHORT).show();
            pd.dismiss();

        }
    }
}