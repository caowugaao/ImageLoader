package com.gx.morgan.imageloaderlib.interfaces;

import android.graphics.Bitmap;

/**
 * description：图片缓存接口
 * <br>author：caowugao
 * <br>time： 2017/09/14 17:02
 */

public interface IImageCache {
    void addToDisk(String url, Bitmap bitmap);

    void addToMemory(String url, Bitmap bitmap);

    void removeInDisk(String url);

    void clearInDisk();

    void release();
}
