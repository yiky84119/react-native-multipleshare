package com.react.multipleshare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultipleShareModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNMultipleShareModule";

    private ReactApplicationContext mContext;
    private HttpUtils mHttpUtils;
    private SparseArray<String> mShareArray = new SparseArray<>();

    public MultipleShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        this.mHttpUtils = new HttpUtils(this.mContext);
    }

    @Override
    public String getName() {
        return "RNMultipleShare";
    }

    @ReactMethod
    public void share(ReadableArray shareArray, final Integer module,
                      final Integer scene, final Promise promise) {

        if (module == Module.QQ) {
            if (Scene.SESSION == scene) {
                if (!isAppAvilible(mContext,"com.tencent.mobileqq")) {
                    promise.reject("1001", "QQ not installed");
                    return;
                }
            } else {
                if (!isAppAvilible(mContext,"com.qzone")) {
                    promise.reject("1002", "QQZone not installed");
                    return;
                }
            }
        } else if (module == Module.WECHAT && !isAppAvilible(mContext,"com.tencent.mm")) {
            promise.reject("1003", "WeChat not installed");
            return;
        }

        ArrayList arrayList = shareArray.toArrayList();
        int index = 0;

        for(Object url : arrayList) {
            String tmp = (String)url;
            if (tmp.contains("http")) {
                mHttpUtils.append((String)url, index);
            } else {
                mShareArray.put(index, (String)url);
            }
            index++;
        }

        mHttpUtils.start(new HttpUtils.HttpUtilsCallback() {
            @Override
            public void onFinish(SparseArray<String> result) {
                Log.i(TAG, "onFinish");

                for (int i = 0; i < result.size(); i++) {
                    int key = result.keyAt(i);
                    mShareArray.put(key, result.valueAt(i));
                }
                doShare(mShareArray, module, scene);
                promise.resolve(true);
                Log.i(TAG, mShareArray.toString());
            }
        });
    }

    private void doShare(final SparseArray<String> array, final int module, final int scene) {
        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                ComponentName comp;
                if (Module.QQ == module){
                    if (Scene.SESSION == scene) {
                        comp = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
                    }else{
                        comp = new ComponentName("com.qzone", "com.qzonex.module.operation.ui.QZonePublishMoodActivity");
                    }
                } else {
                    if (Scene.SESSION == scene) {
                        comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
                    } else {
                        comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                    }
                }
                intent.setComponent(comp);
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");
                ArrayList<Uri> imageUris = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    imageUris.add(Uri.fromFile(new File(array.valueAt(i))));
                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
                mContext.startActivity(intent);
            }
        });
    }

    private boolean isAppAvilible(Context context, String mType) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(mType)) {
                    return true;
                }
            }
        }
        return false;
    }

    class Scene {
        public final static int SESSION = 0;
        public final static int TIMELINE = 1;
    }

    class Module {
        public final static int QQ = 0;
        public final static int WECHAT = 1;
    }
}
