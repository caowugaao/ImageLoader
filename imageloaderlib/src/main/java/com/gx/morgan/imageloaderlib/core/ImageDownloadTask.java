package com.gx.morgan.imageloaderlib.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import com.gx.morgan.imageloaderlib.https.SSLSocketFactoryHelper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * description：图片下载任务 <br>
 * author：caowugao <br>
 * time： 2017/09/14 10:30
 */

public class ImageDownloadTask implements Runnable {

    private static final String TAG = ImageDownloadTask.class.getSimpleName();

    private String imageUrl;

    private static final int CODE_SUCCCESS = 100;

    private static final int CODE_FAIL = 101;

    private ImageView imageView;

    public interface OnImageDownloadListener {
        void onImageRequestSuccess(String url, ImageView imageView, Bitmap bitmap);

        void onImageRequestFail(String url, int code, String msg);

        /**
         * 在子线程中
         * 
         * @param url
         * @param bitmap
         */
        void onSaveToDisk(String url, Bitmap bitmap);
    }

    private MainHandler mainHandler;

    private static class MainHandler extends Handler {
        private WeakReference<ImageDownloadTask> imageDownloaderWeakReference;

        public MainHandler(ImageDownloadTask imageDownloader) {
            super(Looper.getMainLooper());
            this.imageDownloaderWeakReference = new WeakReference<ImageDownloadTask>(imageDownloader);
        }

        @Override
        public void handleMessage(Message msg) {
            ImageDownloadTask task = imageDownloaderWeakReference.get();
            if (null == task) {
                return;
            }
            OnImageDownloadListener listener = task.listener;
            if (null == listener) {
                return;
            }
            switch (msg.what) {
            case CODE_SUCCCESS:
                Bitmap bitmap = (Bitmap) msg.obj;
                listener.onImageRequestSuccess(task.imageUrl, task.imageView, bitmap);
                break;
            case CODE_FAIL:
                int code = msg.arg1;
                String errorString = (String) msg.obj;
                listener.onImageRequestFail(task.imageUrl, code, errorString);
                break;
            }
        }
    }

    private OnImageDownloadListener listener;

    public void setOnDownloadListener(OnImageDownloadListener listener) {
        this.listener = listener;
    }

    public ImageDownloadTask(String imageUrl, ImageView imageView, OnImageDownloadListener listener) {
        this.imageUrl = imageUrl;
        this.imageView = imageView;
        this.listener = listener;
        mainHandler = new MainHandler(this);
    }

    @Override
    public void run() {
        URL imgUrl = null;
        Bitmap bitmap = null;
        int code = 0;
        HttpURLConnection conn = null;
        try {
            imgUrl = new URL(imageUrl);
            if (imageUrl.startsWith("https")) {
                conn = (HttpsURLConnection) imgUrl.openConnection();

                SSLSocketFactory sslSocketFactory = SSLSocketFactoryHelper.getInstance().getDefaultSSLSocketFactory();
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
            } else {
                conn = (HttpURLConnection) imgUrl.openConnection();
            }
            conn.setDoInput(true);
            conn.setConnectTimeout(20 * 1000);
            conn.setReadTimeout(20 * 1000);
            conn.connect();
            code = conn.getResponseCode();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            mainHandler.obtainMessage(CODE_SUCCCESS, bitmap).sendToTarget();
            if (null != listener) {
                listener.onSaveToDisk(imageUrl, bitmap);
            }
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Message message = mainHandler.obtainMessage(CODE_FAIL);
            message.arg1 = code;
            message.obj = e.getMessage();
            message.sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
            Message message = mainHandler.obtainMessage(CODE_FAIL);
            message.arg1 = code;
            message.obj = e.getMessage();
            message.sendToTarget();
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
    }
}
