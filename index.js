import { NativeModules } from 'react-native';

const { configure, isInstalled, startPayment } = NativeModules.RNMobilePay;

const Country = {
  Denmark: 0,
  Norway: 1,
  Finland: 2
}

const CaptureMode = {
  Capture: 0,
  Reserve: 1,
  PartialCapture: 2
}

export default {
  configure,
  isInstalled,
  startPayment,
  Country,
  CaptureMode
}