package com.gx.morgan.imageloaderlib.core;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.gx.morgan.imageloaderlib.interfaces.IDisplay;
import com.gx.morgan.imageloaderlib.interfaces.IImageCache;
import com.gx.morgan.imageloaderlib.interfaces.IImageSource;
import com.gx.morgan.imageloaderlib.interfaces.OnDisplayListener;


/**
 * description：核心抽象工作类
 * <br>author：caowugao
 * <br>time： 2017/09/14 17:16
 */

public abstract class AbstractImageWorker implements IDisplay, IImageSource, IImageCache {
    @Override
    public void display(String imageUrl, ImageView imageView, int width, int height, OnDisplayListener callback) {
        Bitmap fromLocal = getFromLocal(imageUrl, width, height);
        if (null != fromLocal) {
            if (null != callback) {
                Bitmap transform = callback.onTransform(imageUrl, fromLocal);
                if(null!=imageView){
                    imageView.setImageBitmap(transform);
                }
                callback.onImageDisplaySuccess(imageUrl, imageView, transform);
            } else {
                if(null!=imageView){
                    imageView.setImageBitmap(fromLocal);
                }
            }
            return;
        }
        requestNetwork(imageUrl, imageView, callback);
    }

    @Override
    public void display(String imageUrl, ImageView imageView, int width, int height) {
        Bitmap fromLocal = getFromLocal(imageUrl, width, height);
        if (null != fromLocal) {
            if (null != imageView) {
                imageView.setImageBitmap(fromLocal);
            }
            return;
        }
        requestNetwork(imageUrl, imageView);
    }

    private Bitmap getFromLocal(String imageUrl, int width, int height) {
        Bitmap fromMemory = getFromMemory(imageUrl);
        if (null != fromMemory) {
            return fromMemory;
        }
        Bitmap fromDisk = getFromDisk(imageUrl, width, height);
        if (null != fromDisk) {
            addToMemory(imageUrl, fromDisk);
            return fromDisk;
        }
        return null;
    }
    public  abstract void justDownload(String url,ImageView imageView,ImageDownloadTask.OnImageDownloadListener listener);
}
