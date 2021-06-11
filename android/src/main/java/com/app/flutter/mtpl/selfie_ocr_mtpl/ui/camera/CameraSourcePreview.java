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
package com.app.flutter.mtpl.selfie_ocr_mtpl.ui.camera;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.app.flutter.mtpl.selfie_ocr_mtpl.Exif;
import com.app.flutter.mtpl.selfie_ocr_mtpl.FaceTrackerActivity;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "CameraSourcePreview";
    private boolean safeToTakePicture = false;

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }


    public void start(CameraSource cameraSource) throws IOException {
        // Toast.makeText(mContext, "onStart", Toast.LENGTH_SHORT).show();
        if (cameraSource == null) {
            //Toast.makeText(mContext, "Camera Source Null", Toast.LENGTH_SHORT).show();
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() throws IOException {
     //   Toast.makeText(mContext, "StartIfReady", Toast.LENGTH_SHORT).show();
        if (mStartRequested && mSurfaceAvailable) {
        //    Toast.makeText(mContext, "SurfaceAvailbale", Toast.LENGTH_SHORT).show();
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
            //    Toast.makeText(mContext, "Overlay Not Null", Toast.LENGTH_SHORT).show();
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                   // Toast.makeText(mContext, "isPotraitMode", Toast.LENGTH_SHORT).show();
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            //  Toast.makeText(mContext, "Overlay Null", Toast.LENGTH_SHORT).show();
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;


            try {
                startIfReady();
                safeToTakePicture = true;
            } catch (IOException e) {
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
        int width = 320;
        int height = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
//        int childHeight = (int)(((float) layoutWidth / (float) width) * height); // bottom screen cuts view calculation

        int childHeight = layoutHeight; // full screen camera view  // need to set condition as per camera preview

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) height) * width);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }

        try {
            startIfReady();
        } catch (IOException e) {
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        return false;
    }

    public void takeImage() {
        if (safeToTakePicture) {
            mCameraSource.takePicture(null, callbackPicture);
            safeToTakePicture = false;
        }
    }

    CameraSource.PictureCallback callbackPicture = new CameraSource.PictureCallback() {
        public void onPictureTaken(final byte[] data) {

            int orientation = Exif.getOrientation(data);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            switch (orientation) {
                case 90:
                    bitmap = rotateImage(bitmap, 90);

                    break;
                case 180:
                    bitmap = rotateImage(bitmap, 180);

                    break;
                case 270:
                    bitmap = rotateImage(bitmap, 270);

                    break;
                case 0:
                    // if orientation is zero we don't need to rotate this

                default:
                    break;
            }
            //write your code here to save bitmap


            if (mContext instanceof FaceTrackerActivity) {
                FaceTrackerActivity main = (FaceTrackerActivity) mContext;
                main.setImageViewPicture(bitmap);
            }
            safeToTakePicture = true;
        }
    };

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }


}
