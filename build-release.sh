#!/bin/bash
export KEYSTORE_PATH=/home/divyam/fitness/onset-release.keystore
export KEYSTORE_PASSWORD='EKzBYu^%OBsZ4eVV8zB$xGjRloC2Pll6eydhg2q2xURVVCq#TQWZhuhCVePqC#6gr%7OMYrPvufvf1Chn@O5eHr$uTBi4dtlbc4HwGakCNr^M^CDaOZdpp6pN3c9sJV#'
export KEY_ALIAS=onset
export KEY_PASSWORD='EKzBYu^%OBsZ4eVV8zB$xGjRloC2Pll6eydhg2q2xURVVCq#TQWZhuhCVePqC#6gr%7OMYrPvufvf1Chn@O5eHr$uTBi4dtlbc4HwGakCNr^M^CDaOZdpp6pN3c9sJV#'
./gradlew bundleRelease
