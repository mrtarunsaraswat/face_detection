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

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.app.flutter.mtpl.selfie_ocr_mtpl.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    updateEyeBlink eyeBlink;

    private static final int COLOR_CHOICES[] = {
//        Color.BLUE,
//        Color.CYAN,
            Color.TRANSPARENT,
//        Color.MAGENTA,
//        Color.RED,
//        Color.WHITE,
//        Color.YELLOW
    };

   /* private static final int COLOR_CHOICES[] = {
            Color.TRANSPARENT
    };
*/

    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    float overlayX;
    float overlayY;
    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    boolean isFirst = false;
    Activity activity;
    RectF outerAreaRectF;

    //    private ImageView imageViewCircle;
    private ImageView imageViewCircleNew;
    TransparentCircle tcFaceDetectionOverlay;


    public void setImageViewCircle(ImageView imageViewCircleNew) {
//        this.imageViewCircle = imageViewCircle;
        this.imageViewCircleNew = imageViewCircleNew;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            imageViewCircle.setImageDrawable(activity.getResources().getDrawable(R.drawable.rounded_border_white, activity.getTheme()));
//        } else {
//            imageViewCircle.setImageDrawable(activity.getResources().getDrawable(R.drawable.rounded_border_white));
//        }
//        imageViewCircle.setImageDrawable(activity.getDrawable(R.drawable.rounded_border_white));
    }

    public void setTransplantCircle(TransparentCircle tcFaceDetectionOverlay) {
        this.tcFaceDetectionOverlay = tcFaceDetectionOverlay;
    }

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.BLUE);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Activity activity, Face face, float overlayX, float overlayY, updateEyeBlink mUpdateEyeBlink) {
        this.activity = activity;
        mFace = face;
        isFirst = true;
        this.overlayX = overlayX;
        this.overlayY = overlayY;
        postInvalidate();
        eyeBlink = mUpdateEyeBlink;
//        mUpdateEyeBlink.inCircle();

    }

    void updateFace(Activity activity, Face face, RectF outerAreaRectF, updateEyeBlink mUpdateEyeBlink) {
        this.activity = activity;
        mFace = face;
        isFirst = true;
        this.outerAreaRectF = outerAreaRectF;
        postInvalidate();
        eyeBlink = mUpdateEyeBlink;
//        mUpdateEyeBlink.inCircle();

    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + (face.getHeight() / 2));
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.25f);
        float yOffset = scaleY(face.getHeight()/1.75f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        RectF innerRect = new RectF(left, top, right, bottom);

//        canvas.drawOval(innerRect, mBoxPaint);

//        float diffX = overlayX - left;
//        float diffY = overlayY - top;
        float faceWidth = right - left;
        float faceheight = bottom - top;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int diffWidth = (int)(width - (width*.20f));
        int diffHeight = 0;
        if(height/width > 1.75) {
            diffHeight = (int) (height - height * .50f);
        }else {
            diffHeight = (int) (height - height * .40f);
        }

        Log.e("Diff123Width-->",""+diffWidth);
        Log.e("Diff123Hegiht-->",""+diffHeight);

        Log.e("Diff123FaceWidth-->",""+faceWidth);
        Log.e("Diff123FaceHegiht-->",""+faceheight);

        int overlaySizeWidth = dpToPx(diffWidth);
        int overlaySizeHeight = dpToPx(diffHeight);

//        Log.e("DIFF_LEFT","=="+left);
//        Log.e("DIFF_TOP ","=="+top);
//        Log.e("DIFF_X","=="+diffX);
//        Log.e("DIFF_Y","=="+diffY);

        if(outerAreaRectF.contains(innerRect))
        {
            switch (detectFaceInside(diffWidth,diffHeight, faceWidth, faceheight))
            {
                case 0:
                    // Toast.makeText(activity, "Zoom In", Toast.LENGTH_SHORT).show();
//                    imageViewCircleNew.setVisibility(View.INVISIBLE);
                    tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
                    eyeBlink.inCircle(false);
                    break;
                case 1:
                    //  Toast.makeText(activity, "Your face in Circle", Toast.LENGTH_SHORT).show();
//                    imageViewCircleNew.setVisibility(View.VISIBLE);
                    tcFaceDetectionOverlay.changeStrokeColor(Color.GREEN);
                    eyeBlink.inCircle(true);
                    break;
                case 2:
                    // Toast.makeText(activity, "Zoom out", Toast.LENGTH_SHORT).show();
//                    imageViewCircleNew.setVisibility(View.INVISIBLE);
                    tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
                    eyeBlink.inCircle(false);
                    break;
                default:
//                    imageViewCircleNew.setVisibility(View.INVISIBLE);
                    tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
                    eyeBlink.inCircle(false);

            }
        }
        else
        {
            Log.e(" Diff123Circel", "Not Contain");
            tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
            eyeBlink.inCircle(false);
        }

//        if (-250 < diffX &&
//            diffX < 250 &&
//            -400 < diffY &&
//            diffY < 400)
//        {
//            switch (detectFaceInside(overlaySizeWidth,overlaySizeHeight, faceWidth, faceheight))
//            {
//                case 0:
//                   // Toast.makeText(activity, "Zoom In", Toast.LENGTH_SHORT).show();
////                    imageViewCircleNew.setVisibility(View.INVISIBLE);
//                    tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
//                    eyeBlink.inCircle(false);
//                    break;
//                case 1:
//                  //  Toast.makeText(activity, "Your face in Circle", Toast.LENGTH_SHORT).show();
////                    imageViewCircleNew.setVisibility(View.VISIBLE);
//                    tcFaceDetectionOverlay.changeStrokeColor(Color.GREEN);
//                    eyeBlink.inCircle(true);
//                    break;
//                case 2:
//                   // Toast.makeText(activity, "Zoom out", Toast.LENGTH_SHORT).show();
////                    imageViewCircleNew.setVisibility(View.INVISIBLE);
//                    tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
//                    eyeBlink.inCircle(false);
//                    break;
//                default:
////                    imageViewCircleNew.setVisibility(View.INVISIBLE);
//                    tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
//                    eyeBlink.inCircle(false);
//
//            }
//
//        } else {
////            imageViewCircleNew.setVisibility(View.INVISIBLE);
//            tcFaceDetectionOverlay.changeStrokeColor(Color.RED);
//            eyeBlink.inCircle(false);
//        }
    }


    int detectFaceInside(int overlaySizeWidth,int overlaySizeHeight, float faceWidth, float faceHeight) {
        int i = 0;
        float diffWidth = overlaySizeWidth - faceWidth;
        float diffHeight = overlaySizeHeight - faceHeight;
//        Log.e("Diff111_OLWidth","---"+overlaySizeWidth);
//        Log.e("Diff111_OLHeight","---"+overlaySizeHeight);
//        Log.e("Diff111_FCWidth","---"+faceWidth);
//        Log.e("Diff111_FCHeight","---"+faceHeight);
//        Log.e("Diff111_Height","---"+diffHeight);
        int interval = 250;
        if (-interval < diffWidth && diffWidth < interval && -interval < diffHeight && diffHeight < interval) {
            i = 1;
        } else {
            if (diffWidth < -interval && diffHeight < -interval) {
                i = 2;
            } else {
                i = 0;
            }
        }

        return i;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}

interface updateEyeBlink {
    public void inCircle(boolean inCircle);
}