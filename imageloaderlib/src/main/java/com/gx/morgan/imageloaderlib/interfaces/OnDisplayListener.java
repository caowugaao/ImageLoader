package com.gx.morgan.imageloaderlib.interfaces;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * description：图片加载接口
 * <br>author：caowugao
 * <br>time： 2017/09/14 16:52
 */

public interface OnDisplayListener {
    void onImageDisplaySuccess(String url, ImageView imageView, Bitmap bitmap);

    void onImageDisplayFail(String url, int code, String msg);
    
    Bitmap onTransform(String url, Bitmap bitmap);
}
