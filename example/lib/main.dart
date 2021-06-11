import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:selfie_ocr_mtpl/selfie_ocr_mtpl.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterTestSelfiecapture.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Text('Running on: $_platformVersion\n'),
            RaisedButton(
              onPressed: () {
                // CallOCRDetection();
                FlutterTestSelfiecapture.detectLiveliness(
                    "TEst", "msgBlinkEye");
              },
              child: Text("Click"),
            )
          ],
        ),
      ),
    );
  }

  void CallOCRDetection() async {
    String directory = (await getTemporaryDirectory()).path;
    String faceImagePath = "$directory/face_cropped_img.png";
    FlutterTestSelfiecapture.ocrFromDocumentImage(
            imagePath: "/storage/emulated/0/smart_scanner/crop_6667.jpeg",
            destFaceImagePath: faceImagePath,
            xOffset: 30,
            yOffset: 50)
        .then((lines) {
      if (lines != null) {
        final resultData = lines;
        List<dynamic> items = resultData["ExtractedData"];
        print("ImagePath == ${resultData["FaceImagePath"]}");
        print("itemsLength == ${items[0]}");
      }
    });
  }
}
