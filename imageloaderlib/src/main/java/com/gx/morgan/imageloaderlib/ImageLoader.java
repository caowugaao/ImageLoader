package com.gx.morgan.imageloaderlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.gx.morgan.imageloaderlib.core.ImageDownloadTask;
import com.gx.morgan.imageloaderlib.entity.DisplayOptions;
import com.gx.morgan.imageloaderlib.interfaces.OnDisplayListener;
import com.gx.morgan.imageloaderlib.util.ViewUtil;

/**
 * description：
 * <br>author：caowugao
 * <br>time：2018/3/2 17:19
 */
public class ImageLoader {
    private ImageLoader(){}

    private static DisplayOptions defaultDisplayOptions;

    /**
     * 在Application中初始化
     *
     * @param context
     * @param baseDir
     *            为null则默认存放在context.getCacheDir()+images目录
     * @param cacheMemory
     *            最大占用内存
     * @param options
     */
    public static void initCacheDir(Context context, String baseDir, int cacheMemory,DisplayOptions options) {
        RealImageLoader.initCacheDir(context, baseDir, cacheMemory);
        defaultDisplayOptions=options;
    }

    /**
     * 在Application中初始化，默认内存缓存为运行内存1/8
     *
     * @param context
     * @param baseDir
     *            为null则默认存放在context.getCacheDir()+images目录
     *  @param options
     */
    public static void initCacheDir(Context context, String baseDir,DisplayOptions options) {
        RealImageLoader.initCacheDir(context, baseDir);
        defaultDisplayOptions=options;
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
    public static void display(String imageUrl, final ImageView imageView, int width, int height, final OnDisplayListener callback) {

        setPlaceHolder(imageView);

        RealImageLoader.display(imageUrl, imageView, width, height, new OnDisplayListener() {
            @Override
            public void onImageDisplaySuccess(String url, ImageView imageView, Bitmap bitmap) {
                if(null!=callback){
                    callback.onImageDisplaySuccess(url,imageView,bitmap);
                }
            }

            @Override
            public void onImageDisplayFail(String url, int code, String msg) {
                setOnFail(imageView);
                if(null!=callback){
                    callback.onImageDisplayFail(url,code,msg);
                }
            }

            @Override
            public Bitmap onTransform(String url, Bitmap bitmap) {
                return null==callback?bitmap:callback.onTransform(url,bitmap);
            }
        });
    }

    private static void setOnFail(ImageView imageView) {
        if(null!=defaultDisplayOptions){
            if(null!=imageView){
                if(0!=defaultDisplayOptions.failResId){
                    imageView.setImageResource(defaultDisplayOptions.failResId);
                }
                else if(null!=defaultDisplayOptions.failBmp){
                    ViewUtil.setBackground(imageView,defaultDisplayOptions.failBmp);
                }
            }
        }
    }

    private static void setPlaceHolder(ImageView imageView) {
        if(null!=defaultDisplayOptions){
            if(null!=imageView){
                if(0!=defaultDisplayOptions.placeholderResId){
                    imageView.setImageResource(defaultDisplayOptions.placeholderResId);
                }
                else if(null!=defaultDisplayOptions.placeholderBmp){
                    ViewUtil.setBackground(imageView,defaultDisplayOptions.placeholderBmp);
                }
            }
        }
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
        display(imageUrl, imageView, width, height,null);
    }

    /**
     * 按照原图取
     *
     * @param imageUrl
     * @param imageView
     */
    public static void display(String imageUrl, ImageView imageView) {
        display(imageUrl, imageView, 0, 0,null);
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
        RealImageLoader.justDownload(imageUrl, imageView, listener);
    }

    public static void addToDisk(String url, Bitmap bitmap) {
        RealImageLoader.addToDisk(url, bitmap);
    }

    /**
     * 清空缓存数据
     */
    public static void clear() {
        RealImageLoader.clear();
    }

    public static void removeInDisk(String url) {
        RealImageLoader.removeInDisk(url);
    }

    /**
     * 关闭DiskLruCache ，退出应用中调用，在Application.onTerminate()中调用
     */
    public static void release() {
        RealImageLoader.release();
    }
}
