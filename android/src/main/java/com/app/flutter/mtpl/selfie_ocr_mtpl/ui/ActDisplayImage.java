//package com.app.flutter.mtpl.selfie_ocr_mtpl.ui;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.os.Bundle;
//import android.widget.ImageView;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.app.flutter.mtpl.selfie_ocr_mtpl.R;
//
//import java.io.File;
//
///**
// * Created by Jaimin Sarvaiya on 23-10-2019.
// * Copyright (c) 2019 MTPL. All rights reserved.
// */
//public class ActDisplayImage extends AppCompatActivity {
//
//    ImageView imgCapture;
//    String filePath;
//    byte[] data;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.act_image);
//        imgCapture = (ImageView) findViewById(R.id.imgCapture);
//        filePath = getIntent().getStringExtra("image");
//
//        File imgFile = new File(filePath);
//        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//        if (imgFile.exists()) {
//            imgCapture.setImageBitmap(myBitmap);
//        }
//    }
//
//    public static Bitmap rotateImage(Bitmap source, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
//                matrix, true);
//    }
//}
