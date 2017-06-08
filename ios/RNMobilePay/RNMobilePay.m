#import "RNMobilePay.h"
#import "MobilePayManager.h"

@implementation RNMobilePay

MobilePayCountry country = MobilePayCountry_Denmark;
RCTPromiseResolveBlock resolvePayment;
RCTPromiseRejectBlock rejectPayment;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(configure: (NSDictionary *)options)
{
    NSString *merchantId = [options objectForKey:@"merchantId"];
    NSString *urlScheme = [options objectForKey:@"urlScheme"];
    if([options objectForKey:@"country"]) {
        country = [[options objectForKey:@"country"] integerValue];
    }
    [[MobilePayManager sharedInstance] setupWithMerchantId:merchantId merchantUrlScheme:urlScheme country:country];
    
    if([options objectForKey:@"captureType"]) {
        MobilePayCaptureType captureType = [[options objectForKey:@"captureType"] integerValue];
        [MobilePayManager sharedInstance].captureType = captureType;
    }
    if([options objectForKey:@"timeoutSeconds"]) {
        NSInteger timeoutSeconds = [[options objectForKey:@"timeoutSeconds"] integerValue];
        [MobilePayManager sharedInstance].timeoutSeconds = (int)timeoutSeconds;
    }
}

RCT_REMAP_METHOD(isInstalled,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        if ([[MobilePayManager sharedInstance]isMobilePayInstalled:country]) {
            resolve(@YES);
        }
        else {
            resolve(@NO);
        }
    }
    @catch (NSError *error) {
        reject(@"installed_error", @"Could not determine if MobilePay is installed", error);
    }
}

RCT_EXPORT_METHOD(startPayment:(NSString *)orderId
                  price:(float)price
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    MobilePayPayment *payment = [[MobilePayPayment alloc]initWithOrderId:orderId productPrice:price];
    resolvePayment = resolve;
    rejectPayment = reject;
    //No need to start a payment if one or more parameters are missing
    if (payment && (payment.orderId.length > 0) && (payment.productPrice >= 0)) {
        [[MobilePayManager sharedInstance]beginMobilePaymentWithPayment:payment error:^(NSError * _Nonnull error) {
            rejectPayment(@"payment_error", error.localizedFailureReason , error);
        }];
    }
    else {
        NSError *error;
        if(payment.orderId.length == 0) {
            rejectPayment(@"no_orderid", @"No order id provided", error);
        }
        else if (payment.productPrice < 0) {
            rejectPayment(@"negative_price", @"Product price cannot be negative", error);
        }
    }
}

- (void)handleMobilePayPaymentWithUrl:(NSURL *)url
{
    [[MobilePayManager sharedInstance]handleMobilePayPaymentWithUrl:url success:^(MobilePaySuccessfulPayment * _Nullable mobilePaySuccessfulPayment) {
        NSString *orderId = mobilePaySuccessfulPayment.orderId;
        NSString *transactionId = mobilePaySuccessfulPayment.transactionId;
        NSDecimalNumber *amountWithDrawnFromCard = (NSDecimalNumber *)[NSDecimalNumber numberWithFloat:mobilePaySuccessfulPayment.amountWithdrawnFromCard];
        NSDecimalNumberHandler *behavior = [NSDecimalNumberHandler decimalNumberHandlerWithRoundingMode:NSRoundPlain
                                                                                                  scale:2
                                                                                       raiseOnExactness:NO
                                                                                        raiseOnOverflow:NO
                                                                                       raiseOnUnderflow:NO
                                                                                    raiseOnDivideByZero:NO];
        amountWithDrawnFromCard = [amountWithDrawnFromCard decimalNumberByRoundingAccordingToBehavior:behavior];
        
        NSDictionary *paymentDict = [NSDictionary dictionaryWithObjectsAndKeys:
                                     orderId, @"orderId",
                                     transactionId, @"transactionId",
                                     amountWithDrawnFromCard, @"amountWithdrawnFromCard",
                                     nil];
        resolvePayment(paymentDict);
        return;
    } error:^(NSError * _Nonnull error) {
        rejectPayment([NSString stringWithFormat:@"%d", (int)error.code], @"Payment error", error);
        return;
    } cancel:^(MobilePayCancelledPayment * _Nullable mobilePayCancelledPayment) {

        NSError *error;
        rejectPayment(@"payment_cancelled", @"Payment Cancelled", error);
        return;
    }];
}

@end
