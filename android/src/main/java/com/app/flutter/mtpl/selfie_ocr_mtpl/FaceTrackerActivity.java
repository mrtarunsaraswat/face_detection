/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.app.flutter.mtpl.selfie_ocr_mtpl;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.flutter.mtpl.selfie_ocr_mtpl.ui.camera.CameraSourcePreview;
import com.app.flutter.mtpl.selfie_ocr_mtpl.ui.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private ImageView imgOverlay;
    private ImageView imgOverlayNew;
    TransparentCircle tcFaceDetectionOverlay;
    int heightDiff = 0;
    int widthDiff = 0;

    Handler mHandler;
    AlertDialog dialog;
    String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
    File mFileTemp;
    //==============================================================================================
    // Activity Methods
    //==============================================================================================
    private final double OPEN_THRESHOLD = 0.85;
    private final double CLOSE_THRESHOLD = 0.15;
    private int state = 0;

    private static final int PERMISSION_REQUEST_CODE = 200;
    ImageView imgClose;
    TextView txtBlinkEye;
    TextView txtMsg;
    String msgselfieCapture,msgBlinkEye;
    Intent intent;

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);


        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }

        intent = getIntent();
        msgselfieCapture = intent.getStringExtra("msgselfieCapture");
        msgBlinkEye = intent.getStringExtra("msgBlinkEye");

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        imgOverlay = (ImageView) findViewById(R.id.imgOverlay);
        imgOverlayNew = (ImageView) findViewById(R.id. imgNew);
        tcFaceDetectionOverlay = (TransparentCircle) findViewById(R.id.tcFaceDetectionOverlay);
        imgClose = (ImageView) findViewById(R.id.imgClose);
        txtMsg = (TextView) findViewById(R.id.txtMsg);
        txtBlinkEye = (TextView) findViewById(R.id.txtBlinkEye);

        txtMsg.setText(msgselfieCapture);
        txtBlinkEye.setText(msgBlinkEye);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FaceTrackerActivity.this.finish();
                overridePendingTransition(0, R.anim.push_out_to_bottom);
            }
        });

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.

       /* int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();

        }*/


        if (!checkPermission()) {

            requestPermission();

        } else {
            createCameraSource();
        }


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
//        int width = size.x;
//        int height = size.y;

        if (imgOverlay != null) {
            imgOverlay.getX();
            imgOverlay.getY();
        }

        if (imgOverlayNew != null) {
            imgOverlayNew.getX();
            imgOverlayNew.getY();
        }


    }

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] + grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted :)
                    createCameraSource();
                } else {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, CAMERA) ||
//                            ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, WRITE_EXTERNAL_STORAGE) ||
//                            ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, READ_EXTERNAL_STORAGE)) {
//                        requestPermission();
//                    } else {
                    showDialogPermission();
                    // }
                }
                break;
        }
    }

    public void showDialogPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FaceTrackerActivity.this);
        builder.setMessage(getResources().getString(R.string.txt_need_camera_storage_permission))
                //.setTitle(getResources().getString(R.string.app_name))
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        FaceTrackerActivity.this.finish();
                        overridePendingTransition(0, R.anim.push_out_to_bottom);
                    }
                })
                .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        dialog.dismiss();
                        startActivityForResult(i, 2);

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            createCameraSource();
        } else {
            //requestPermission();
            if (!checkPermission()) {
                showDialogPermission();
            } else {
                createCameraSource();
            }
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(true)
                .build();
        //Toast.makeText(FaceTrackerActivity.this, "CreateCameraSource", Toast.LENGTH_SHORT).show();

        //        BoxDetector boxDetector = new BoxDetector(detector, 500, 500);
        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            // Toast.makeText(FaceTrackerActivity.this, "CreateCameraSource", Toast.LENGTH_SHORT).show();
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
        mCameraSource = new CameraSource.Builder(context, detector)
//                .setRequestedPreviewSize(displayMetrics.heightPixels, displayMetrics.widthPixels)
//                .setRequestedPreviewSize(1280, 720)
                .setRequestedPreviewSize(1280, 720)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        // need to set condition as per camera preview

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
 /*   @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }


        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
*/
    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                //  Toast.makeText(this, "Camera Source not null", Toast.LENGTH_SHORT).show();
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void setImageViewPicture(Bitmap bmpPicture) {
        if (persistImage(bmpPicture) == null) return;
        File f1 = new File(persistImage(bmpPicture).getPath());
        Intent intent = new Intent();
        intent.putExtra("filePath",f1.toString());
        setResult(Activity.RESULT_OK,intent);
        FaceTrackerActivity.this.finish();
        FaceTrackerActivity.this.overridePendingTransition(0, R.anim.push_out_to_bottom);
       /* Intent in1 = new Intent(this, ActDisplayImage.class);
        in1.putExtra("image", f1.toString());
        startActivity(in1);*/
    }

    private File persistImage(Bitmap bitmap) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }
        int file_size = 0;
        if(mFileTemp!=null) {
            file_size = Integer.parseInt(String.valueOf(mFileTemp.length() / 1024000));
        }
        OutputStream os;
        try {
            os = new FileOutputStream(mFileTemp);
            if(file_size >= 5) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            }

            os.flush();
            os.close();
            return mFileTemp;
        } catch (Exception e) {
        }
        return null;
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        boolean isBlink = false;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
//            mFaceGraphic.setImageViewCircle(imgOverlayNew);
            mFaceGraphic.setTransplantCircle(tcFaceDetectionOverlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(final FaceDetector.Detections<Face> detectionResults, final Face face) {
            mOverlay.add(mFaceGraphic);
            float width = tcFaceDetectionOverlay.rectF.right - tcFaceDetectionOverlay.rectF.left;
            float height = tcFaceDetectionOverlay.rectF.bottom - tcFaceDetectionOverlay.rectF.top;
            Log.e("DIFF_WIDTH ","=="+width);
            Log.e("DIFF_HEIGHT  ","=="+height);
            mFaceGraphic.updateFace(FaceTrackerActivity.this, face,tcFaceDetectionOverlay.rectF, new updateEyeBlink() {
                @Override
                public void inCircle(boolean inCircle) {
                    /**
                     * Eye blink start
                     * */
                    if (inCircle) {
                        txtBlinkEye.setVisibility(View.VISIBLE);
                        if (detectionResults.getDetectedItems() != null &&
                                detectionResults.getDetectedItems().size() != 0) {
                            for (int i = 0; i < detectionResults.getDetectedItems().size(); i++) {
                                int key = detectionResults.getDetectedItems().keyAt(i);


                                if (detectionResults.getDetectedItems().get(key) != null &&
                                        detectionResults.getDetectedItems().get(key).getId() == face.getId()) {
                                    Face mFace = detectionResults.getDetectedItems().get(key);


                                    float left = mFace.getIsLeftEyeOpenProbability();
                                    float right = mFace.getIsRightEyeOpenProbability();
                                    if ((left == Face.UNCOMPUTED_PROBABILITY) ||
                                            (right == Face.UNCOMPUTED_PROBABILITY)) {
                                        // Toast.makeText(FaceTrackerActivity.this, "Eyes are not detected", Toast.LENGTH_SHORT).show();
                                        // At least one of the eyes was not detected.
                                        return;
                                    }

                                    switch (state) {
                                        case 0:
                                            if ((left > OPEN_THRESHOLD) && (right > OPEN_THRESHOLD)) {
                                                // Both eyes are initially open
                                                state = 1;

                                            }
                                            break;

                                        case 1:
                                            if ((left < CLOSE_THRESHOLD) && (right < CLOSE_THRESHOLD)) {
                                                // Both eyes become closed
                                                state = 2;
                                            }
                                            break;

                                        case 2:
                                            if ((left > OPEN_THRESHOLD) && (right > OPEN_THRESHOLD)) {
                                                // Both eyes are open again
                                                mPreview.takeImage();
                                                state = 0;
                                                inCircle = false;
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }else{
                        txtBlinkEye.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }


        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}
