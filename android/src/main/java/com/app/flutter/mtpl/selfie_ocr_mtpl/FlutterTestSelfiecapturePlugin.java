package com.app.flutter.mtpl.selfie_ocr_mtpl;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
/**
 * FlutterTestSelfiecapturePlugin
 */
public class FlutterTestSelfiecapturePlugin implements MethodCallHandler {
    /**
     * Plugin registration.
     */
    Activity context;
    MethodChannel methodChannel;
    public static Result result;
    private PluginRegistry.Registrar registrar;
    private SelfieDelegate delegate;
    public FlutterTestSelfiecapturePlugin(final PluginRegistry.Registrar registrar, final SelfieDelegate delegate) {
        this.registrar = registrar;
        this.delegate = delegate;
    }
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "selfie_ocr_mtpl");
        SelfieDelegate delegate =
                new SelfieDelegate(registrar.activity());
        registrar.addActivityResultListener(delegate);
        channel.setMethodCallHandler(new FlutterTestSelfiecapturePlugin(registrar, delegate));
    }
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (registrar.activity() == null) {
            result.error("no_activity", "edge_detection plugin requires a foreground activity.", null);
            return;
        }
        else if (call.method.equalsIgnoreCase("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equalsIgnoreCase("detectLiveliness")) {
            this.result = result;
            delegate.detectLivelinesss(call, result);
        } else if (call.method.equalsIgnoreCase("ocrFromDocImage")) {
            this.result = result;
                extractDataFromImage(call.argument("imagePath").toString(),
                call.argument("destFaceImagePath").toString(),
                Integer.parseInt(call.argument("xOffset").toString()),
                Integer.parseInt(call.argument("yOffset").toString())
            );
        }else {
            result.notImplemented();
        }
    }

    public void extractDataFromImage(String imagePath,String destFaceImagePath,int xOffset,int yOffset){
        List<String> items = new ArrayList<String>();
        String faceImagePath = "";
        Map<String,Object> resultOfOCR = new HashMap();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(registrar.activity()).build();
        if(textRecognizer.isOperational()){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();

            SparseArray<TextBlock>  blocks =  textRecognizer.detect(frame);

            if(blocks!=null)
            {
                for(int i=0;i<blocks.size();i++){
                    items.add(blocks.valueAt(i).getValue());
                }
            }
            textRecognizer.release();
        }

        /* Extract Face Image from Document*/
        FaceDetector faceDetector = new FaceDetector.Builder(registrar.activity())
            .setTrackingEnabled(false)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .build();

        if(faceDetector.isOperational()){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame);

            if(faces!=null && faces.size()!=0)
            {
                if(faces.valueAt(0)!=null){
                    int xValue = (int) faces.valueAt(0).getPosition().x - xOffset;
                    if(xValue<0){
                        xValue = 0;
                    }
                    int yValue = (int) faces.valueAt(0).getPosition().y - xOffset;
                    if(yValue<0){
                        yValue = 0;
                    }
                    int width = (int) faces.valueAt(0).getWidth() + yOffset;
                    int height = (int) faces.valueAt(0).getHeight() + yOffset;

                    Bitmap faceBitmap = Bitmap.createBitmap(bitmap,xValue,yValue,width,height);

                    File f = new File(destFaceImagePath);
                    try{
                        if(f.exists()){
                            f.createNewFile();
                        }
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        faceBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                        faceImagePath = f.getAbsolutePath();
                    }catch (IOException e){
                        e.getMessage();
                        result.error("1010","Exception",null);
                    }

                }else{
                    result.error("1010","Exception",null);
                }

            }
            faceDetector.release();
        }

        resultOfOCR.put("ExtractedData",items);
        resultOfOCR.put("FaceImagePath",faceImagePath);

        result.success(resultOfOCR);
    }


}
