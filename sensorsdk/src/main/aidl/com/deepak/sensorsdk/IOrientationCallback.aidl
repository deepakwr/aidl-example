// IOrientationCallback.aidl
package com.deepak.sensorsdk;

// Declare any non-default types here with import statements

interface IOrientationCallback {
    oneway void orientationCallback(in float[] values);

}
