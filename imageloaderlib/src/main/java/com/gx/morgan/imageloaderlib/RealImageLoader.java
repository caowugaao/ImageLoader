package com.gx.morgan.imageloaderlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.gx.morgan.imageloaderlib.core.ImageDownloadTask;
import com.gx.morgan.imageloaderlib.core.ImageWorker;
import com.gx.morgan.imageloaderlib.interfaces.OnDisplayListener;


/**
 * description： <br>
 * author：caowugao <br>
 * time： 2017/09/14 16:17
 */

 class RealImageLoader {
    private RealImageLoader() {
    }

    /**
     * 在Application中初始化
     * 
     * @param context
     * @param baseDir
     *            为null则默认存放在context.getCacheDir()+images目录
     * @param cacheMemory
     *            最大占用内存
     */
    public static void initCacheDir(Context context, String baseDir, int cacheMemory) {
        ImageWorker.getInstance().initCacheDir(context, baseDir, cacheMemory);
    }

    /**
     * 在Application中初始化，默认内存缓存为运行内存1/8
     * 
     * @param context
     * @param baseDir
     *            为null则默认存放在context.getCacheDir()+images目录
     */
    public static void initCacheDir(Context context, String baseDir) {
        ImageWorker.getInstance().initCacheDir(context, baseDir);
    }

    /**
     * @param imageUrl
     * @param imageView
     * @param width
     *            0则表示按照原图取
     * @param height
     *            0则表示按照原图取
     * @param callback
     */
    public static void display(String imageUrl, ImageView imageView, int width, int height, OnDisplayListener callback) {
        ImageWorker.getInstance().display(imageUrl, imageView, width, height, callback);
    }

    /**
     * @param imageUrl
     * @param imageView
     * @param width
     *            0则表示按照原图取
     * @param height
     *            0则表示按照原图取
     */
    public static void display(String imageUrl, ImageView imageView, int width, int height) {
        ImageWorker.getInstance().display(imageUrl, imageView, width, height);
    }

    /**
     * 按照原图取
     * 
     * @param imageUrl
     * @param imageView
     */
    public static void display(String imageUrl, ImageView imageView) {
        display(imageUrl, imageView, 0, 0);
    }

    /**
     * 按照原图取
     * 
     * @param imageUrl
     * @param imageView
     * @param callback
     */
    public static void display(String imageUrl, ImageView imageView, OnDisplayListener callback) {
        display(imageUrl, imageView, 0, 0, callback);
    }

    /**
     * @param imageUrl
     * @param imageView
     * @param listener
     */
    public static void justDownload(String imageUrl, ImageView imageView, ImageDownloadTask.OnImageDownloadListener listener) {
        ImageWorker.getInstance().justDownload(imageUrl, imageView, listener);
    }

    public static void addToDisk(String url, Bitmap bitmap) {
        ImageWorker.getInstance().addToDisk(url, bitmap);
    }

    /**
     * 清空缓存数据
     */
    public static void clear() {
        ImageWorker.getInstance().clearInDisk();
    }

    public static void removeInDisk(String url) {
        ImageWorker.getInstance().removeInDisk(url);
    }

    /**
     * 关闭DiskLruCache ，退出应用中调用，在Application.onTerminate()中调用
     */
    public static void release() {
        ImageWorker.getInstance().release();
    }
}
