package com.example.smedcan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.Interfaces.JavascriptInterface;
import com.example.smedcan.data.model.Patients;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import kotlin.jvm.internal.Intrinsics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Doc_Home_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Doc_Home_Fragment extends Fragment {
    AppCompatTextView txt;
    WebView webView;
    ImageView accept,reject,togglevideo,toggleaudio;
    NestedScrollView inputlayout;
    RelativeLayout relativeLayout;
    LinearLayout linearLayout;
    TextView calltxt;
    String touser;
    private String uniqueId;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

    boolean isAudio = true;
    boolean isVideo = true;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Doc_Home_Fragment() {
        // Required empty public constructor
    }

    public static Doc_Home_Fragment newInstance(String param1, String param2) {
        Doc_Home_Fragment fragment = new Doc_Home_Fragment();
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
        View view = inflater.inflate(R.layout.fragment_dochome, container, false);
        txt=view.findViewById(R.id.wait);
        webView=view.findViewById(R.id.webView);
        accept=view.findViewById(R.id.acceptBtn);
        reject=view.findViewById(R.id.rejectBtn);
        toggleaudio=view.findViewById(R.id.toggleAudioBtn);
        togglevideo=view.findViewById(R.id.toggleVideoBtn);
        relativeLayout=view.findViewById(R.id.callLayout);
        inputlayout=view.findViewById(R.id.inputLayout);
        linearLayout=view.findViewById(R.id.callControlLayout);
        calltxt=view.findViewById(R.id.incomingCallTxt);
        touser = "p"+Doctor_Home.uname;
        togglevideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo=!isVideo;
                callJavascriptFunction("javascript:toggleAudio(\"" + isVideo + "\")");
                if (isVideo){
                    togglevideo.setImageResource(R.drawable.ic_baseline_videocam_24);
                }else{
                    togglevideo.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                }
            }
        });
        toggleaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio=!isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio){
                    toggleaudio.setImageResource(R.drawable.ic_baseline_mic_24);
                }else{
                    toggleaudio.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }
            }
        });

        setupWebView();

        return view;
    }

    private final void callJavascriptFunction(final String functionString) {
        webView.post((Runnable)(new Runnable() {
            public final void run() {
                webView.evaluateJavascript(functionString, (ValueCallback)null);
            }
        }));
    }

    private void setupWebView() {
        webView.setWebChromeClient((WebChromeClient)(new WebChromeClient() {
            public void onPermissionRequest(@Nullable PermissionRequest request) {
                if (request != null) {
                    request.grant(request.getResources());
                }

            }
        }));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        webView.addJavascriptInterface(new JavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);
        webView.setWebViewClient((WebViewClient)(new WebViewClient() {
            public void onPageFinished(@Nullable WebView view, @Nullable String url) {
                initializePeer();
            }
        }));
    }

    private final void initializePeer() {
        this.uniqueId = this.getUniqueID();
        this.callJavascriptFunction("javascript:init(\"" + this.uniqueId + "\")");
//        reference.child(fromuser).child("incoming").addValueEventListener((ValueEventListener)(new ValueEventListener() {
//            public void onCancelled(@NotNull DatabaseError error) {
//
//            }
//
//            public void onDataChange(@NotNull DataSnapshot snapshot) {
//                Intrinsics.checkParameterIsNotNull(snapshot, "snapshot");
//                Object var10001 = snapshot.getValue();
//                if (!(var10001 instanceof String)) {
//                    var10001 = null;
//                }
//
//                onCallRequest((String)var10001);
//            }
//        }));
    }

    private final String getUniqueID() {
        String var10000 = UUID.randomUUID().toString();
        return var10000;
    }

    private void onCallRequest(String caller) {
        if (caller != null) {
            relativeLayout.setVisibility(View.VISIBLE);
            calltxt.setText((CharSequence)(caller + " is calling..."));
            accept.setOnClickListener((View.OnClickListener)(new View.OnClickListener() {
                public final void onClick(View it) {
//                    reference.child(fromuser).child("connId").setValue(uniqueId);
//                    reference.child(fromuser).child("isAvailable").setValue(true);
                    relativeLayout.setVisibility(View.GONE);
                    switchToControls();
                }
            }));
            reject.setOnClickListener((View.OnClickListener)(new View.OnClickListener() {
                public final void onClick(View it) {
//                    reference.child(fromuser).child("incoming").setValue((Object)null);
//                    relativeLayout.setVisibility(View.GONE);
                }
            }));
        }
    }

    private void switchToControls() {
        inputlayout.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
    }
}