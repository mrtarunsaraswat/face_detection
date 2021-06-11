#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'selfie_ocr_mtpl'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin which detect liveness and capture the selfie'
  s.description      = <<-DESC
A new Flutter plugin which detect liveness and capture the selfie
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.ios.dependency 'GoogleMobileVision/FaceDetector'
  s.ios.dependency 'GoogleMobileVision/MVDataOutput'
  s.ios.dependency 'GoogleMobileVision/TextDetector'

  s.ios.deployment_target = '11.1'
  s.static_framework = true
  
  s.source_files = 'Classes/**/*.{h,m,swift,xcdatamodeld,storyboard}'
  s.resource_bundles = {
    'ResourceBundleName' => ['path/to/resources/*/**']
  }
#  s.resources = 'XDCoreLib/Pod/Resources/**/*.{png,storyboard}'
  s.resource = 'Classes/*.storyboard'
#  s.resources = 'Classes/*.png'
#  s.resource = 'Classes/Assets.xcassets'
 
end

