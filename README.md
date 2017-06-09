# React Native MobilePay AppSwitch

This is an unofficial module for React Native wrapping the Danske Bank MobilePay AppSwitch SDK for iOS and Android.

This module is not developed, published or otherwise affiliated with Danske Bank or MobilePay. It is a private project released "as-is" to the public.

Note that at this time MobilePay is only supported in Denmark, Norway and Finland.

## Installation

Start by installing the package from npm:
```
npm install react-native-mobilepay-appswitch --save
```
Then link the module:
```
react-native link react-native-mobilepay-appswitch
```

### iOS

For iOS there are a couple of additional installation steps:

##### 1. URL schemes

Setup URL schemes in your app as explained in the [MobilePay Getting Started Guide](https://github.com/MobilePayDev/MobilePay-AppSwitch-SDK/wiki/Getting-Started-on-iPhone):

> The AppSwitch SDK supports three countries, which corresponds to respective versions of the MobilePay app, and each app has a distinct url scheme:
>
> MobilePay Denmark: mobilepay://
>
> MobilePay Norway: mobilepayno://
>
>MobilePay Finland: mobilepayfi://
>
> Open your info.plist find "URL types" entry and add a "URL Identifier" entry with a value matching your bundle identifier fx. "com.trifork.MobilePayFruitShop".
> 
> Then add a "URL Schemes" Item with a name matching your app. In this example we use "fruitshop", and you should not add this to your production app - this is just an example! This is the part that enables opening your app from an url.
>
> In this case you are now able to open a browser on your iPhone and type in "fruitshop://" and your app will open.
>
> As of Xcode 7 and iOS9 SDK you must also whitelist the MobilePay URL scheme for the SDK (your merchant app) in order for it to be able to open the MobilePay app. This must be added to your info.plist.

##### 2. AppDelegate

Add the following to the top of your `AppDelegate.m`:
```
#import <RNMobilePay.h>
```
Then add the below method just before `@end`:

```
-(BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options
{
    RNMobilePay *mobilePay=[[RNMobilePay alloc] init];
    [mobilePay handleMobilePayPaymentWithUrl:url];
    return YES;
}
```

## Usage

Import the module in your React Native component:
```
import MobilePay from 'react-native-mobilepay-appswitch'
```

### Methods

##### `configure(options: Object)`

You must configure MobilePay before you call other methods. The `configure` method takes an `options` object with the following properties:

| Property | Type | Required | Default value | Description |
| ------ | ------ | ------ | ------ | ------ |
| `merchantId` | `String` | Yes | None | Your MobilePay MerchantID. You can get IDs for testing [here](https://github.com/MobilePayDev/MobilePay-AppSwitch-SDK#merchantid-for-test-purposes).
| `urlScheme` | `String` | iOS only | None | The URL scheme of your app.
| `country` | `MobilePay.Country` | No | `MobilePay.Country.Denmark` | MobilePay is currently only supported in Denmark, Norway and Finland.
| `captureType` | `MobilePay.CaptureType` | No | `MobilePay.CaptureType.Capture` | Read more about payment types [here](https://github.com/MobilePayDev/MobilePay-AppSwitch-SDK/wiki/Payment-types).
| `timeoutSeconds` | `Number` | | No | `0` (Never timeout) | A time limit you set for which the user must have swiped in MobilePay to confirm the purchase.

__Example__

```
MobilePay.configure({
  merchantId: 'YOUR_MERCHANTID',
  urlScheme: 'YOUR_APP_URL_SCHEME',
  country: MobilePay.Country.Denmark,
  captureType: MobilePay.CaptureType.Capture,
  timeoutSeconds: 90
})
```


##### `isInstalled(): Promise`

Use this method to determine whether the user has the MobilePay app installed.

__Example__

```
MobilePay.isInstalled()
  .then(installed => {
    console.log(installed);
  })
  .catch(error => {
    console.log(error);
  })
```


##### `startPayment(orderId: String, amount: Number): Promise`

If the payment is successful the promise will resolve with an object containing the following properties:

| Property | Type | Description |
| ------ | ------ | ------ |
| `orderId` | `String` | The order ID from your app. |
| `transactionId` | `String` | The payment transaction ID from MobilePay. |
| `amountWithdrawnFromCard` | `Number` | The amount withdrawn from the user's card. If using `MobilePay.CaptureType.Reserve` this is the amount reserved on the card.

__Example__

```
MobilePay.startPayment('123456', 3.14)
  .then(payment => {
    console.log(payment);
  })
  .catch(error => {
    console.log(error);
  })
```

### Types

##### Country
* `MobilePay.Country.Denmark` (alternatively `0`)
* `MobilePay.Country.Norway` (alternatively `1`)
* `MobilePay.Country.Finland` (alternatively `2`)

##### Capture Type
* `MobilePay.CaptureType.Capture` (alternatively `0`)
* `MobilePay.CaptureType.Reserve` (alternatively `1`)
* `MobilePay.CaptureType.PartialCapture` (alternatively `2`)







