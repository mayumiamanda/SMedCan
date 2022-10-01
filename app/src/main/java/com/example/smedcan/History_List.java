package com.example.smedcan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.adapters.cus_history_adapter;
import com.example.smedcan.data.model.AppointmentModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link History_List#newInstance} factory method to
 * create an instance of this fragment.
 */
public class History_List extends Fragment {
    cus_history_adapter adapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public History_List() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment History_List.
     */
    // TODO: Rename and change types and number of parameters
    public static History_List newInstance(String param1, String param2) {
        History_List fragment = new History_List();
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
        View view = inflater.inflate(R.layout.fragment_history__list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.history_list);
        FragmentManager fragmentManager = getFragmentManager();
        adapter = new cus_history_adapter(getActivity(), fragmentManager,getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        String url = Conf.serverurl + "/loadappointments";
        RequestParams params = new RequestParams();
        params.put("did", Doctor_Home.uname);
        new AsyncHttpClient().get(url,params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response=new String(responseBody);
                try {
                    JSONArray jsonArr = new JSONArray(response);
                    ArrayList<AppointmentModel> d_list = new ArrayList<>();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(i);
//                        System.out.println(jsonObj);
                        AppointmentModel ap=new AppointmentModel();
                        ap.setIdappointments(Integer.parseInt(jsonObj.get("idappointments").toString()));
                        ap.setDate(jsonObj.get("date").toString());
                        ap.setTime(jsonObj.get("time").toString());
                        ap.setUsername(jsonObj.get("username").toString());
                        ap.setEmail(jsonObj.get("email").toString());
                        ap.setPrice(jsonObj.get("price").toString());
                        d_list.add(ap);
                    }
                    adapter.loadHistoryList(d_list);
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
}