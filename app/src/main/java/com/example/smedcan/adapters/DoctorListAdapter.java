package com.example.smedcan.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smedcan.CallActivity;
import com.example.smedcan.Config.Conf;
import com.example.smedcan.Doctor_Home;
import com.example.smedcan.Doctor_list;
import com.example.smedcan.Home;
import com.example.smedcan.R;
import com.example.smedcan.RegisterActivity;
import com.example.smedcan.data.model.Doctor;
import com.example.smedcan.dialogs.DoctorProfileDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitPreapprovalRequest;
import lk.payhere.androidsdk.model.InitRequest;

public class DoctorListAdapter  extends RecyclerView.Adapter<DoctorListAdapter.ViewHolder> {
    private ArrayList d_list;
    private Context context;
    private Activity activity;
    private FragmentManager fragmentManager;
    private final String[] permissions = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    private final int requestcode = 1;
    final static int PAYHERE_REQUEST = 10010;
    public static String docid;

    public DoctorListAdapter(Context context, FragmentManager fragmentManager,Activity activity){
        d_list = new ArrayList();
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.activity=activity;
    }

    public void loadDoctorList(ArrayList output) {
        this.d_list = output;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_details, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doc = (Doctor) d_list.get(position);
        holder.docname.setText(doc.getDname());
        holder.doctype.setText(doc.getDtype());
        holder.price.setText("Rs. "+doc.getPrice().toString());
        if (doc.getImgurl()!=null){
            Picasso.get().load(doc.getImgurl()).into(holder.image);
        }
        holder.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPermissionGranted()) {
                    askPermissions();
                }else{
//                    InitRequest req = new InitRequest();
//                    req.setMerchantId("1218545");       // Your Merchant PayHere ID
//                    req.setMerchantSecret("8RjoddBG8Xt8m228MoqUwU4jlUqLis4Bx8LQwpnuZUVo"); // Your Merchant secret (Add your app at Settings > Domains & Credentials, to get this))
//                    req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
//                    req.setAmount(1000.00);             // Final Amount to be charged
//                    req.setOrderId("230000151");        // Unique Reference ID
//                    req.setItemsDescription("Door bell wireless");  // Item description title
//                    req.setCustom1("This is the custom message 1");
//                    req.setCustom2("This is the custom message 2");
//                    req.getCustomer().setFirstName("Saman");
//                    req.getCustomer().setLastName("Perera");
//                    req.getCustomer().setEmail("samanp@gmail.com");
//                    req.getCustomer().setPhone("+94771234567");
//                    req.getCustomer().getAddress().setAddress("No.1, Galle Road");
//                    req.getCustomer().getAddress().setCity("Colombo");
//                    req.getCustomer().getAddress().setCountry("Sri Lanka");
//
//
//                    Intent intent = new Intent(activity, PHMainActivity.class);
//                    intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
//                    PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
//                    activity.startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID like private final static int PAYHERE_REQUEST = 11010;
//
//                    docid=String.valueOf(doc.getDid());


                }
            }
        });
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("did", "" + doc.getDid());
                DoctorProfileDialog dialog=new DoctorProfileDialog();
                dialog.setArguments(bundle);
                dialog.show(fragmentManager, "profile_dialog");

            }
        });
    }

    @Override
    public int getItemCount() {
        return d_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView docname,doctype,price;
        Button submit;
        CircleImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            docname=itemView.findViewById(R.id.docname);
            doctype=itemView.findViewById(R.id.doc_type);
            price=itemView.findViewById(R.id.price);
            submit=itemView.findViewById(R.id.btn_request);
            image=itemView.findViewById(R.id.image_profile_Request);

        }
    }

    private final void askPermissions() {
        ActivityCompat.requestPermissions(activity, this.permissions, this.requestcode);
    }

    private final boolean isPermissionGranted() {
        boolean grant=true;
        for (String s: permissions) {
            int res =context.checkCallingOrSelfPermission(s);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                grant=false;
            }
        }

        return  grant;
    }


}
