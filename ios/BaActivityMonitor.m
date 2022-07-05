#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(BaActivityMonitor, RCTEventEmitter)

RCT_EXTERN_METHOD(askPermissionIOS:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(start:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject);

RCT_EXTERN_METHOD(stop);

RCT_EXTERN_METHOD(isStarted);

RCT_EXTERN_METHOD(sendMockActivities:(NSArray *)activities);


+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
