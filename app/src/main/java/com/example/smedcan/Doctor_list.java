package com.example.smedcan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.adapters.DoctorListAdapter;
import com.example.smedcan.data.model.Doctor;
import com.example.smedcan.data.model.Patients;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.StatusResponse;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Doctor_list#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Doctor_list extends Fragment {
    DoctorListAdapter adapter;
    final static int PAYHERE_REQUEST = 10010;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Doctor_list() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Doctor_list.
     */
    // TODO: Rename and change types and number of parameters
    public static Doctor_list newInstance(String param1, String param2) {
        Doctor_list fragment = new Doctor_list();
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
        View view = inflater.inflate(R.layout.fragment_doctor_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.docdata);
        FragmentManager fragmentManager = getFragmentManager();
        adapter = new DoctorListAdapter(getActivity(), fragmentManager, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        String url = Conf.serverurl + "/loaddoctorlist";
        new AsyncHttpClient().get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                try {
                    JSONArray jsonArr = new JSONArray(response);
                    ArrayList<Doctor> d_list = new ArrayList<>();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(i);
                        System.out.println(jsonObj);
                        Doctor d = new Doctor();
                        d.setDid(Integer.parseInt(jsonObj.get("did").toString()));
                        d.setDname(jsonObj.get("dname").toString());
                        d.setDtype(jsonObj.get("dtype").toString());
                        if (!jsonObj.get("imgurl").toString().equals("")){
                            d.setImgurl(jsonObj.get("imgurl").toString());
                        }
                        d.setPrice(Double.parseDouble(jsonObj.get("price").toString()));
                        d_list.add(d);
                    }
                    adapter.loadDoctorList(d_list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null)
                    if (response.isSuccess()) {
                        msg = "Activity result:" + response.getData().toString();
                        String url = Conf.serverurl + "/makeappointment";
                        RequestParams params = new RequestParams();
                        params.put("did", DoctorListAdapter.docid);
                        params.put("pname", Home.uname);
                        new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String response=new String(responseBody);
                                if (response.equals("success")){
                                    Intent intent=new Intent(getActivity(), CallActivity.class);
                                    intent.putExtra("did",DoctorListAdapter.docid);
                                    intent.putExtra("user","patient");
                                    getActivity().startActivity(intent);
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(), "Unable to make appointment", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
                                error.printStackTrace();
                            }
                        });
                    }else {
                        msg = "Result:" + response.toString();
                    }else {
                    msg = "Result: no response";
                }
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {
                    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getActivity(), "User cancelled the request", Toast.LENGTH_SHORT).show();
            }
        }
    }
}