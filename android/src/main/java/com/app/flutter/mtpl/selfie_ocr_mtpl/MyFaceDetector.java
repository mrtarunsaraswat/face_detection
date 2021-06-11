package com.app.flutter.mtpl.selfie_ocr_mtpl;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

class MyFaceDetector extends Detector<Face> {
  private Detector<Face> mDelegate;

  MyFaceDetector(Detector<Face> delegate) {
    mDelegate = delegate;
  }

  public SparseArray<Face> detect(Frame frame) {
    // *** add your custom frame processing code here
    return mDelegate.detect(frame);
  }

  public boolean isOperational() {
    return mDelegate.isOperational();
  }

  public boolean setFocus(int id) {
    return mDelegate.setFocus(id);
  }
}