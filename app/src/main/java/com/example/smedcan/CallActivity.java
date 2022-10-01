package com.example.smedcan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smedcan.Config.Conf;
import com.example.smedcan.Interfaces.JavascriptInterface;
import com.example.smedcan.data.model.Doctor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import kotlin.jvm.internal.Intrinsics;


public class CallActivity extends AppCompatActivity {
    String fromuser;
    String touser;
    String crruser;
    String getCaller;
    private String uniqueId;

    boolean isPeerConnected = false;
    WebView webView;
    ImageView accept, reject, togglevideo, toggleaudio, endbtn;
    RelativeLayout relativeLayout, inputLayout;
    LinearLayout linearLayout;
    TextView calltxt;
    Button callbtn;

    // High-accuracy landmark detection and face classification
    FaceDetectorOptions highAccuracyOpts;
    public ArrayList<Float> smile;
    public ArrayList<Float> eyeopen;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

    boolean isAudio = true;
    boolean isVideo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        webView = findViewById(R.id.webView);
        accept = findViewById(R.id.acceptBtn);
        reject = findViewById(R.id.rejectBtn);
        endbtn = findViewById(R.id.endBtn);
        toggleaudio = findViewById(R.id.toggleAudioBtn);
        togglevideo = findViewById(R.id.toggleVideoBtn);
        callbtn = findViewById(R.id.callBtn);
        relativeLayout = findViewById(R.id.callLayout);
        inputLayout = findViewById(R.id.inputLayout);
        linearLayout = findViewById(R.id.callControlLayout);
        calltxt = findViewById(R.id.incomingCallTxt);

        highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        smile = new ArrayList();
        eyeopen = new ArrayList();
        touser = "d" + getIntent().getStringExtra("did");
        crruser = getIntent().getStringExtra("user");
        fromuser = "p" + Home.uname;
        togglevideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");
                if (isVideo) {
                    togglevideo.setImageResource(R.drawable.ic_baseline_videocam_24);
                } else {
                    togglevideo.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                }
            }
        });
        toggleaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio) {
                    toggleaudio.setImageResource(R.drawable.ic_baseline_mic_24);
                } else {
                    toggleaudio.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }
            }
        });
        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(touser).setValue(null);
//                onDestroy();
            }
        });

        if (crruser.equals("doctor")) {
            callbtn.setVisibility(View.GONE);
        } else {
            callbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendCallRequest();
                }
            });
        }
        setupWebView();

    }

    private void setupWebView() {
        webView.setWebChromeClient((WebChromeClient) (new WebChromeClient() {
            public void onPermissionRequest(@Nullable PermissionRequest request) {
                if (request != null) {
                    request.grant(request.getResources());
                }

            }
        }));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new JavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);
        webView.setWebViewClient((WebViewClient) (new WebViewClient() {
            public void onPageFinished(@Nullable WebView view, @Nullable String url) {
                initializePeer();
            }
        }));
    }

    private final void initializePeer() {
        this.uniqueId = this.getUniqueID();
        this.callJavascriptFunction("javascript:init(\"" + this.uniqueId + "\")");
        reference.child(touser).child("incoming").addValueEventListener((ValueEventListener) (new ValueEventListener() {
            public void onCancelled(@NotNull DatabaseError error) {

            }

            public void onDataChange(@NotNull DataSnapshot snapshot) {
                Object var10001 = snapshot.getValue();
                if (!(var10001 instanceof String)) {
                    var10001 = null;
                }
                if (var10001 != null) {
                    if (crruser.equals("doctor")) {
                        onCallRequest((String) var10001);
                    }
                } else {
                    onDestroy();
                }
            }
        }));
    }

    private void onCallRequest(String caller) {
        if (fromuser.substring(1).equals("null")) {
            getCaller = caller;
        }
        if (caller != null) {
            relativeLayout.setVisibility(View.VISIBLE);
            calltxt.setText((CharSequence) (caller.substring(1) + " is calling..."));
            accept.setOnClickListener((View.OnClickListener) (new View.OnClickListener() {
                public final void onClick(View it) {
                    reference.child(touser).child("connId").setValue(uniqueId);
                    reference.child(touser).child("isAvailable").setValue(true);
                    relativeLayout.setVisibility(View.GONE);
                    switchToControls();
                }
            }));
            reject.setOnClickListener((View.OnClickListener) (new View.OnClickListener() {
                public final void onClick(View it) {
                    reference.child(touser).child("incoming").setValue((Object) null);
                    relativeLayout.setVisibility(View.GONE);
                }
            }));
        }
    }

    private void switchToControls() {
        inputLayout.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You're not connected. Check your internet", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "You're connected.", Toast.LENGTH_LONG).show();
            caputurePhoto();
            reference.child(touser).child("incoming").setValue(fromuser);
            reference.child(touser).child("isAvailable").addValueEventListener((ValueEventListener) (new ValueEventListener() {
                public void onCancelled(@NotNull DatabaseError error) {
                    Intrinsics.checkParameterIsNotNull(error, "error");
                }

                public void onDataChange(@NotNull DataSnapshot snapshot) {
                    if (Intrinsics.areEqual(String.valueOf(snapshot.getValue()), "true")) {
                        listenForConnId();
                    }

                }
            }));
        }
    }

    private void listenForConnId() {
        reference.child(touser).child("connId").addValueEventListener((ValueEventListener) (new ValueEventListener() {
            public void onCancelled(@NotNull DatabaseError error) {
                Intrinsics.checkParameterIsNotNull(error, "error");
            }

            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    switchToControls();
                    callJavascriptFunction("javascript:startCall(\"" + snapshot.getValue() + "\")");
                }
            }
        }));
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private final String getUniqueID() {
        String var10000 = UUID.randomUUID().toString();
        return var10000;
    }

    private final void callJavascriptFunction(final String functionString) {
        webView.post((Runnable) (new Runnable() {
            public final void run() {
                webView.evaluateJavascript(functionString, (ValueCallback) null);
            }
        }));
    }

//    public void endCall(){
//        String url = Conf.serverurl + "/endcall";
//        RequestParams params = new RequestParams();
//        if (fromuser.substring(1).equals("null")){
//            params.put("pname", getCaller.substring(1));
//        }else{
//            params.put("pname", fromuser.substring(1));
//        }
//        params.put("did", touser.substring(1));
//
//        new AsyncHttpClient().post(url, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String response=new String(responseBody);
//                if (response.equals("success")){
//                    reference.child(touser).setValue(null);
//                    webView.loadUrl("about:blank");
//                }else{
//                    Toast.makeText(getApplicationContext(), "Failed to end call", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(getApplicationContext(), "Something went wrong. please try again " + statusCode, Toast.LENGTH_SHORT).show();
//                error.printStackTrace();
//            }
//        });
//    }

    private void caputurePhoto() {
        try {
            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            File imagesFolder;
            String FolderName = "SMEDCAN";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + FolderName);
            } else {
                imagesFolder = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);
            }
            if (!imagesFolder.exists())
                imagesFolder.mkdirs(); // <----
            File image = new File(imagesFolder, System.currentTimeMillis()
                    + ".jpg");
            System.out.println(image.getAbsolutePath().toString());

            FileOutputStream outputStream = new FileOutputStream(image);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            findImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findImage(File image) {
        // remember close de FileOutput
        if (Build.VERSION.SDK_INT < 19)
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        else {
            MediaScannerConnection
                    .scanFile(
                            getApplicationContext(),
                            new String[]{image.toString()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(
                                        String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned "
                                            + path + ":");
                                    Log.i("ExternalStorage", "-> uri="
                                            + uri);
                                    InputImage image = null;
                                    try {
                                        image = InputImage.fromFilePath(getApplicationContext(), uri);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    detectFace(image);
                                }
                            });
        }

    }

    private void detectFace(InputImage image) {
        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
// Or use the default options:
// FaceDetector detector = FaceDetection.getClient();
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_face_info]
                                        for (Face face : faces) {
                                            Rect bounds = face.getBoundingBox();
                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
                                            System.out.println("rotY= " + rotY);
                                            System.out.println("rotZ= " + rotZ);
                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):
                                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                PointF leftEarPos = leftEar.getPosition();
                                                System.out.println("leftEarPos= " + leftEarPos);
                                            }

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != null) {
                                                float smileProb = face.getSmilingProbability();
                                                smile.add(smileProb);
                                                System.out.println("smileProb= " + smileProb);
                                            }
                                            if (face.getRightEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                eyeopen.add(rightEyeOpenProb);
                                                System.out.println("rightEyeOpenProb= " + rightEyeOpenProb);
                                            }

                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != null) {
                                                int id = face.getTrackingId();
                                                System.out.println("getTrackingId= " + id);
                                            }
                                        }
                                        // [END get_face_info]
                                        // [END_EXCLUDE]
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        System.out.println("failed to analyze image");
                                    }
                                });
        // [END run_detector]
    }

    @Override
    public void onBackPressed() {
        reference.child(touser).setValue(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}