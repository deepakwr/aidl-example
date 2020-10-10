// IOrientationInterface.aidl
package com.deepak.sensorsdk;

// Declare any non-default types here with import statements
import com.deepak.sensorsdk.IOrientationCallback;

interface IOrientationInterface {

    float[] getOrientationData();

    void getOrientationValues(IOrientationCallback callback);

}
