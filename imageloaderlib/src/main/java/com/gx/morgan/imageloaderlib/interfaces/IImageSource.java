package com.gx.morgan.imageloaderlib.interfaces;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * description：图片数据来源接口
 * <br>author：caowugao
 * <br>time： 2017/09/14 16:57
 */

public interface IImageSource {
    Bitmap getFromMemory(String url);

    Bitmap getFromDisk(String url, int width, int height);

    void requestNetwork(final String imageUrl, ImageView imageView);

    void requestNetwork(final String imageUrl, ImageView imageView, final OnDisplayListener callback);
}
