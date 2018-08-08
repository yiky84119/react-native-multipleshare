package com.react.multipleshare;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class MultipleShareModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNMultipleShareModule";

    public MultipleShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNMultipleShareModule";
    }
}
