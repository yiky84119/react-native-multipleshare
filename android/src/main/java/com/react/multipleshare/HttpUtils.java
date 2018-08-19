package com.react.multipleshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils implements NextTask {
    private static OkHttpClient singleton;
    private ArrayList<Integer> mQueue = new ArrayList<>();
    private SparseArray<Call> mQueueHandle = new SparseArray<>();
    private SparseArray<String> mQueueResult = new SparseArray<>();
    private static final int MAX_WIDTH = 720;
    private static final int MAX_HEIGHT = 1280;
    private static final int MAX_RUNNING_TASK = 2;
    private HttpUtilsCallback mHttpUtilsCallback = null;
    private int mCount = 0;
    private int mIndex = 0;
    private int mHandledCount = 0;
    private String mFilePrefix;
    private Context mContext;
    private File mFile;


    public HttpUtils(Context context) {
        this.mContext = context;
    }

    public static OkHttpClient getInstance() {
        if (singleton == null) {
            synchronized (HttpUtils.class) {
                if (singleton == null) {
                    singleton = new OkHttpClient();
                }
            }
        }
        return singleton;
    }

    public void append(String url, int index) {
        Request request = new Request.Builder().url(url).build();
        Call call = getInstance().newCall(request);
        mQueue.add(index);
        mQueueHandle.put(index, call);
    }

    public void start(HttpUtilsCallback callback) {
        this.mHttpUtilsCallback = callback;
        mFile = new File(mContext.getExternalCacheDir(), "MShareCache");
        if (!mFile.isDirectory()) {
            mFile.mkdir();
        } else {
            File[] files = mFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                f.delete();
            }
        }
        this.mFilePrefix = Long.toString(new Date().getTime());
        this.mQueueResult.clear();
        mCount = 0;
        mIndex = 0;
        mHandledCount = 0;
        handleNext();
    }

    public synchronized void handleNext() {
        while(mCount < MAX_RUNNING_TASK && mIndex < mQueue.size()) {
            ++mCount;
            int index = mQueue.get(mIndex++);
            Call call = mQueueHandle.get(index);
            call.enqueue(new HttpCallback(new File(mFile,
                    this.mFilePrefix + "_" + Integer.toString(mIndex) + ".jpg"), index,this));
        }

        if (mQueue.size() == mHandledCount) {
            this.mQueue.clear();
            this.mQueueHandle.clear();
            this.mHttpUtilsCallback.onFinish(mQueueResult);
        }
    }

    public synchronized void next(int index, String path) {
        mHandledCount++;
        mCount--;
        mQueueResult.put(index, path);
        handleNext();
    }

    @Override
    public void doNextTask(int index, String path) {
        next(index, path);
    }

    public interface HttpUtilsCallback {
        void onFinish(SparseArray<String> result);
    }

    public class HttpCallback implements Callback {
        private NextTask mNextTask;
        private File mPath;
        private int mPos;

        public HttpCallback(File path, int pos, NextTask nextTask) {
            super();
            this.mNextTask = nextTask;
            this.mPath = path;
            this.mPos = pos;
        }

        public int calculateInSampleSize(BitmapFactory.Options options,
                                                int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int heightRatio = Math.round((float) height
                        / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);

                inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
            }

            return inSampleSize;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            this.mNextTask.doNextTask(this.mPos, null);
        }

        @Override
        public void onResponse(Call call, Response response) {
            InputStream is = null;
            try {
                is = response.body().byteStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while((len = is.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                byteArrayOutputStream.flush();
                InputStream stream1 = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                stream1.close();
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream1, null, ops);
                ops.inSampleSize = calculateInSampleSize(ops, MAX_WIDTH, MAX_HEIGHT);
                ops.inJustDecodeBounds = false;
                InputStream stream2 = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, ops);
                stream2.close();
                FileOutputStream outStream = new FileOutputStream(mPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.mNextTask.doNextTask(this.mPos, mPath.getAbsolutePath());
            mPath = null;
        }
    }
}
