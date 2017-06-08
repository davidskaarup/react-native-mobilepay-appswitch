#import <React/RCTBridgeModule.h>

@interface RNMobilePay : NSObject <RCTBridgeModule>

- (void)handleMobilePayPaymentWithUrl:(NSURL *)url;

@end
