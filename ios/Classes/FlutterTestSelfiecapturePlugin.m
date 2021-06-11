#import "FlutterTestSelfiecapturePlugin.h"
#import <selfie_ocr_mtpl/selfie_ocr_mtpl-Swift.h>

@implementation FlutterTestSelfiecapturePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterTestSelfiecapturePlugin registerWithRegistrar:registrar];
}
@end
