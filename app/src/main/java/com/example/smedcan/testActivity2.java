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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class testActivity2 extends AppCompatActivity {

    // High-accuracy landmark detection and face classification
    FaceDetectorOptions highAccuracyOpts;
    public ArrayList<Float> smile;
    public ArrayList<Float> eyeopen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        smile = new ArrayList();
        eyeopen = new ArrayList();
       /* Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);*/
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (int i = 0; i <= 5; i++) {
//                        System.out.println("Attemptingggggggggggg " + i);
//                        Thread.sleep(3000);
//
//                        caputurePhoto();
//                    }
//                }catch(InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        caputurePhoto();
    }

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

    public float getSmileAnalyze() {
        float average_smile =0;
        if (smile.size() > 0) {
            float total_smile = 0;
            for (float s : smile) {
                total_smile += s;
            }
            average_smile=total_smile/smile.size();
        }
        return average_smile;
    }

    public float getEyeopenAnalyze() {
        float average_eyeopen =0;
        if (eyeopen.size() > 0) {
            float total_eyeopen = 0;
            for (float eo : eyeopen) {
                total_eyeopen += eo;
            }
            average_eyeopen=total_eyeopen/eyeopen.size();
        }
        return average_eyeopen;
    }
    public String getFinalAnalyze(){
        float smileAnalyze = getSmileAnalyze();
        float eyeopenAnalyze = getEyeopenAnalyze();
        String smileResult="";
        String eyeOpenResult="";
        if (smileAnalyze<0.25){
            smileResult="Barely smiling";
        }else if (smileAnalyze<0.5){
            smileResult="Average smiling";
        }else if (smileAnalyze<0.75){
            smileResult="Normally smiling";
        }
        if (eyeopenAnalyze<0.25){
            eyeOpenResult="Eyes barely open";
        }else if (eyeopenAnalyze<0.5){
            eyeOpenResult="Eyes average open";
        }else if (eyeopenAnalyze<0.75){
            eyeOpenResult="Eyes normally open";
        }
        return "Patient is "+smileResult+" and "+eyeOpenResult;
    } 
}