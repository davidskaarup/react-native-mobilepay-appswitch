package com.RNMobilePay;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.math.BigDecimal;

import dk.danskebank.mobilepay.sdk.CaptureType;
import dk.danskebank.mobilepay.sdk.Country;
import dk.danskebank.mobilepay.sdk.MobilePay;
import dk.danskebank.mobilepay.sdk.ResultCallback;
import dk.danskebank.mobilepay.sdk.model.FailureResult;
import dk.danskebank.mobilepay.sdk.model.Payment;
import dk.danskebank.mobilepay.sdk.model.SuccessResult;

public class RNMobilePayModule extends ReactContextBaseJavaModule {
    public int MOBILEPAY_PAYMENT_REQUEST_CODE = 0;
    private Promise paymentPromise;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == MOBILEPAY_PAYMENT_REQUEST_CODE) {
                MobilePay.getInstance().handleResult(resultCode, intent, new ResultCallback() {
                    @Override
                    public void onSuccess(SuccessResult result) {
                        WritableMap map = Arguments.createMap();
                        map.putString("transactionId", result.getTransactionId());
                        map.putString("orderid", result.getOrderId());
                        map.putDouble("amountWithdrawnFromCard", result.getAmountWithdrawnFromCard().doubleValue());
                        paymentPromise.resolve(map);
                    }

                    @Override
                    public void onFailure(FailureResult result) {
                        String errorCode = String.valueOf(result.getErrorCode());
                        String errorMessage = result.getErrorMessage();
                        paymentPromise.reject(errorCode, errorMessage);
                    }

                    @Override
                    public void onCancel() {
                        paymentPromise.reject("payment_cancelled", "Payment Cancelled");
                    }
                });

                paymentPromise = null;
            }
        }
    };

    ReactApplicationContext reactContext;

    public RNMobilePayModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "RNMobilePay";
    }

    @ReactMethod
    public void configure(ReadableMap options) {
        String merchantId = options.getString("merchantId");
        Integer countryInt = options.hasKey("country") ? options.getInt("country") : 0;
        Integer captureTypeInt = options.hasKey("captureType") ? options.getInt("captureType") : 0;
        Integer timeoutSeconds = options.hasKey("timeoutSeconds") ? options.getInt("timeoutSeconds") : 0;

        Country country = Country.DENMARK;
        if (countryInt == 1) country = Country.NORWAY;
        else if (countryInt == 2) country = Country.FINLAND;

        CaptureType captureType = CaptureType.CAPTURE;
        if (captureTypeInt == 1) captureType = CaptureType.RESERVE;
        else if (captureTypeInt == 2) captureType = CaptureType.PARTIAL_CAPTURE;

        MobilePay.getInstance().init(merchantId, country);
        MobilePay.getInstance().setCaptureType(captureType);
        MobilePay.getInstance().setTimeoutSeconds(timeoutSeconds);
    }

    @ReactMethod
    public void isInstalled(Promise promise) {
        try {
            boolean isMobilePayInstalled = MobilePay.getInstance().isMobilePayInstalled(reactContext);
            if (isMobilePayInstalled) {
                promise.resolve(true);
            }
            else {
                promise.resolve(false);
            }
        }
        catch (RuntimeException e) {
            promise.reject("installed_error", "Could not determine if MobilePay is installed", e);
        }
    }

    @ReactMethod
    public void startPayment(String orderId, Float amount, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        paymentPromise = promise;
        boolean isMobilePayInstalled = MobilePay.getInstance().isMobilePayInstalled(reactContext);
        if (isMobilePayInstalled) {
            Payment payment = new Payment();
            payment.setProductPrice(new BigDecimal(amount));
            payment.setOrderId(orderId);

            Intent paymentIntent = MobilePay.getInstance().createPaymentIntent(payment);
            currentActivity.startActivityForResult(paymentIntent, MOBILEPAY_PAYMENT_REQUEST_CODE);
        }
        else {
            paymentPromise.reject("not_installed", "MobilePay is not installed");
        }
    }
}