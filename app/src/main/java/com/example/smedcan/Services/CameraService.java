package com.example.smedcan.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.util.List;

public class CameraService extends Service implements
        SurfaceHolder.Callback{

    // Camera variables
    // a surface holder
    // a variable to control the camera
    private android.hardware.Camera mCamera;
    // the camera parameters
    private Camera.Parameters parameters;
    private Bitmap bmp;
    FileOutputStream fo;
    private String FLASH_MODE;
    private int QUALITY_MODE = 0;
    private boolean isFrontCamRequest = false;
    private android.hardware.Camera.Size pictureSize;
    SurfaceView sv;
    private SurfaceHolder sHolder;
    private WindowManager windowManager;
    WindowManager.LayoutParams params;
    public Intent cameraIntent;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int width = 0, height = 0;
    // High-accuracy landmark detection and face classification
    FaceDetectorOptions highAccuracyOpts ;

    // Real-time contour detection
    FaceDetectorOptions realTimeOpts ;

    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        super.onCreate();
        highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        realTimeOpts =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();
    }

    private Camera openFrontFacingCameraGingerbread() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Camera",
                            "Camera failed to open: " + e.getLocalizedMessage());
                    /*
                     * Toast.makeText(getApplicationContext(),
                     * "Front Camera failed to open", Toast.LENGTH_LONG)
                     * .show();
                     */
                }
            }
        }
        return cam;
    }

    private void setBesttPictureResolution() {
        // get biggest picture size
        width = pref.getInt("Picture_Width", 0);
        height = pref.getInt("Picture_height", 0);

        if (width == 0 | height == 0) {
            pictureSize = getBiggesttPictureSize(parameters);
            if (pictureSize != null)
                parameters
                        .setPictureSize(pictureSize.width, pictureSize.height);
            // save width and height in sharedprefrences
            width = pictureSize.width;
            height = pictureSize.height;
            editor.putInt("Picture_Width", width);
            editor.putInt("Picture_height", height);
            editor.commit();

        } else {
            // if (pictureSize != null)
            parameters.setPictureSize(width, height);
        }
    }

    private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** Check if this device has front camera */
    private boolean checkFrontCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FRONT)) {
            // this device has front camera
            return true;
        } else {
            // no front camera on this device
            return false;
        }
    }

    Handler handler = new Handler();

    private class TakeImage extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... params) {
            takeImage(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    private synchronized void takeImage(Intent intent) {

        if (checkCameraHardware(getApplicationContext())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String flash_mode = extras.getString("FLASH");
                FLASH_MODE = flash_mode;

                boolean front_cam_req = extras.getBoolean("Front_Request");
                isFrontCamRequest = front_cam_req;

                int quality_mode = extras.getInt("Quality_Mode");
                QUALITY_MODE = quality_mode;
            }

            if (isFrontCamRequest) {

                // set flash 0ff
                FLASH_MODE = "off";
                // only for gingerbread and newer versions
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {

                    mCamera = openFrontFacingCameraGingerbread();
                    if (mCamera != null) {

                        try {
                            mCamera.setPreviewDisplay(sv.getHolder());
                        } catch (IOException e) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "API dosen't support front camera",
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                            stopSelf();
                        }
                        Camera.Parameters parameters = mCamera.getParameters();
                        pictureSize = getBiggesttPictureSize(parameters);
                        if (pictureSize != null)
                            parameters
                                    .setPictureSize(pictureSize.width, pictureSize.height);

                        // set camera parameters
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        mCamera.takePicture(null, null, mCall);

                        // return 4;

                    } else {
                        mCamera = null;
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Your Device dosen't have Front Camera !",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                        stopSelf();
                    }
                    /*
                     * sHolder = sv.getHolder(); // tells Android that this
                     * surface will have its data // constantly // replaced if
                     * (Build.VERSION.SDK_INT < 11)
                     *
                     * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
                     */
                } else {
                    if (checkFrontCamera(getApplicationContext())) {
                        mCamera = openFrontFacingCameraGingerbread();

                        if (mCamera != null) {

                            try {
                                mCamera.setPreviewDisplay(sv.getHolder());
                            } catch (IOException e) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "API dosen't support front camera",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                                stopSelf();
                            }
                            Camera.Parameters parameters = mCamera.getParameters();
                            pictureSize = getBiggesttPictureSize(parameters);
                            if (pictureSize != null)
                                parameters
                                        .setPictureSize(pictureSize.width, pictureSize.height);

                            // set camera parameters
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                            mCamera.takePicture(null, null, mCall);
                            // return 4;

                        } else {
                            mCamera = null;
                            /*
                             * Toast.makeText(getApplicationContext(),
                             * "API dosen't support front camera",
                             * Toast.LENGTH_LONG).show();
                             */
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Your Device dosen't have Front Camera !",
                                            Toast.LENGTH_LONG).show();

                                }
                            });

                            stopSelf();

                        }
                        // Get a surface
                        /*
                         * sHolder = sv.getHolder(); // tells Android that this
                         * surface will have its data // constantly // replaced
                         * if (Build.VERSION.SDK_INT < 11)
                         *
                         * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
                         * );
                         */
                    }

                }

            } else {

                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = Camera.open();
                } else
                    mCamera = getCameraInstance();

                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(sv.getHolder());
                        parameters = mCamera.getParameters();
                        if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
                            FLASH_MODE = "auto";
                        }
                        parameters.setFlashMode(FLASH_MODE);
                        // set biggest picture
                        setBesttPictureResolution();
                        // log quality and image format
                        Log.d("Qaulity", parameters.getJpegQuality() + "");
                        Log.d("Format", parameters.getPictureFormat() + "");

                        // set camera parameters
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                        Log.d("ImageTakin", "OnTake()");
                        mCamera.takePicture(null, null, mCall);
                    } else {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Camera is unavailable !",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                    // return 4;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("TAG", "CmaraHeadService()::takePicture", e);
                }
                // Get a surface
                /*
                 * sHolder = sv.getHolder(); // tells Android that this surface
                 * will have its data constantly // replaced if
                 * (Build.VERSION.SDK_INT < 11)
                 *
                 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                 */

            }

        } else {
            // display in long period of time
            /*
             * Toast.makeText(getApplicationContext(),
             * "Your Device dosen't have a Camera !", Toast.LENGTH_LONG)
             * .show();
             */
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Your Device dosen't have a Camera !",
                            Toast.LENGTH_LONG).show();
                }
            });
            stopSelf();
        }

        // return super.onStartCommand(intent, flags, startId);

    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // sv = new SurfaceView(getApplicationContext());
        cameraIntent = intent;
        Log.d("ImageTakin", "StartCommand()");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = 1;
        params.height = 1;
        params.x = 0;
        params.y = 0;
        sv = new SurfaceView(getApplicationContext());

        windowManager.addView(sv, params);
        sHolder = sv.getHolder();
        sHolder.addCallback(this);

        // tells Android that this surface will have its data constantly
        // replaced
        if (Build.VERSION.SDK_INT < 11)
            sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        return 1;
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the data obtained by the camera into a Bitmap
            Log.d("ImageTaking", "Done");
            if (bmp != null)
                bmp.recycle();
            System.gc();
            bmp = decodeBitmap(data);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (bmp != null && QUALITY_MODE == 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
            else if (bmp != null && QUALITY_MODE != 0)
                bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, bytes);
            File imagesFolder;
            String FolderName="SMEDCAN";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + FolderName);
            }
            else
            {
                imagesFolder = new File(Environment.getExternalStorageDirectory() + "/" + FolderName);
            }
//            File imagesFolder = new File(
//                    getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DCIM), "SMEDCAN");
            if (!imagesFolder.exists())
                imagesFolder.mkdirs(); // <----
            File image = new File(imagesFolder, System.currentTimeMillis()
                    + ".jpg");
            System.out.println(image.getAbsolutePath().toString());
            // write the bytes in file
            try {
                fo = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                Log.e("TAG", "FileNotFoundException", e);
                // TODO Auto-generated catch block
            }
            try {
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                Log.e("TAG", "fo.write::PictureTaken", e);
                // TODO Auto-generated catch block
            }

            // remember close de FileOutput
            try {
                fo.close();
                if (Build.VERSION.SDK_INT < 19)
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                else {
                    MediaScannerConnection
                            .scanFile(
                                    getApplicationContext(),
                                    new String[] { image.toString() },
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(
                                                String path, Uri uri) {
                                            Log.i("ExternalStorage", "Scanned "
                                                    + path + ":");
                                            Log.i("ExternalStorage", "-> uri="
                                                    + uri);
                                            InputImage image=null;
                                            try {
                                                image = InputImage.fromFilePath(getApplicationContext(), uri);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            detectFace(image);
                                        }
                                    });
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            /*
             * Toast.makeText(getApplicationContext(),
             * "Your Picture has been taken !", Toast.LENGTH_LONG).show();
             */
            Log.d("Camera", "Image Taken !");
            if (bmp != null) {
                bmp.recycle();
                bmp = null;
                System.gc();
            }
            mCamera = null;
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                                    "Your Picture has been taken !", Toast.LENGTH_SHORT)
                            .show();
                }
            });
            stopSelf();
        }
    };

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
                                            System.out.println("rotY= "+rotY);
                                            System.out.println("rotZ= "+rotZ);
                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):
                                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                PointF leftEarPos = leftEar.getPosition();
                                                System.out.println("leftEarPos= "+leftEarPos);
                                            }

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != null) {
                                                float smileProb = face.getSmilingProbability();
                                                System.out.println("smileProb= "+smileProb);
                                            }
                                            if (face.getRightEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                System.out.println("rightEyeOpenProb= "+rightEyeOpenProb);
                                            }

                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != null) {
                                                int id = face.getTrackingId();
                                                System.out.println("getTrackingId= "+id);
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
                                    }
                                });
        // [END run_detector]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onDestroy() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (sv != null)
            windowManager.removeView(sv);
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (cameraIntent != null)
            new TakeImage().execute(cameraIntent);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public static Bitmap decodeBitmap(byte[] data) {

        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false; // Disable Dithering mode
        bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
        // memory, the Bitmap can be cleared
        bfOptions.inInputShareable = true; // Which kind of reference will be
        // used to recover the Bitmap data
        // after being clear, when it will
        // be used in the future
        bfOptions.inTempStorage = new byte[32 * 1024];

        if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bfOptions);

        return bitmap;
    }

}
