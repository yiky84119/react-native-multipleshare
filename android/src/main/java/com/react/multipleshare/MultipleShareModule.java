package com.react.multipleshare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultipleShareModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNMultipleShareModule";

    private ReactApplicationContext mContext;
    private HttpUtils mHttpUtils;
    private SparseArray<File> mShareArray = new SparseArray<>();

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

        mShareArray.clear();
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

        String filePrefix = Long.toString(new Date().getTime());

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MShareCache");
        if (!dir.isDirectory()) {
            dir.mkdir();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    f.delete();
                }
            }
        }

        for(Object url : arrayList) {
           String tmp = (String)url;
            if (tmp.startsWith("http")) {
                mHttpUtils.append((String)url, index);
            } else {
                File newFile = new File(dir,filePrefix + "_" + Integer.toString(index) + ".jpg");
                try
                {
                    File tmpFile = new File(Uri.parse(tmp).getPath());

                    InputStream fosfrom = new FileInputStream(tmpFile);
                    OutputStream fosto = new FileOutputStream(newFile);
                    byte bt[] = new byte[1024];
                    int c;
                    while ((c = fosfrom.read(bt)) > 0)
                    {
                        fosto.write(bt, 0, c);
                    }
                    fosfrom.close();
                    fosto.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                mShareArray.put(index, newFile);
            }
            index++;
        }

        mHttpUtils.start(new HttpUtils.HttpUtilsCallback() {
            @Override
            public void onFinish(SparseArray<File> result) {
                Log.i(TAG, "onFinish");

                for (int i = 0; i < result.size(); i++) {
                    int key = result.keyAt(i);
                    mShareArray.put(key, result.valueAt(i));
                }
                if (mShareArray.size() == 0) {
                    promise.reject("1000", "无可分享的内容");
                    return;
                }
                doShare(mShareArray, module, scene);
                promise.resolve(true);
                Log.i(TAG, mShareArray.toString());
            }
        }, dir, filePrefix);
    }

    private void doShare(final SparseArray<File> array, final int module, final int scene) {
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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        imageUris.add(Uri.fromFile(array.valueAt(i)));
                    } else {
                        //修复微信在7.0崩溃的问题
                        Uri uri = null;
                        try {
                            uri = Uri.parse(android.provider.MediaStore.Images.Media.
                                    insertImage(mContext.getContentResolver(), array.valueAt(i).getAbsolutePath(),
                                            array.valueAt(i).getName(), null));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        imageUris.add(uri);
                    }
                    //imageUris.add(Uri.fromFile(array.valueAt(i)));
                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                Log.i(TAG, imageUris.toString());
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
