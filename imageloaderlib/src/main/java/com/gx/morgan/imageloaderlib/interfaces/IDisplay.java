package com.gx.morgan.imageloaderlib.interfaces;

import android.widget.ImageView;

/**
 * description：图片展示接口
 * <br>author：caowugao
 * <br>time： 2017/09/14 16:44
 */

public interface IDisplay {
    void display(String imageUrl, ImageView imageView, int width, int height, OnDisplayListener callback);
    void display(String imageUrl, ImageView imageView, int width, int height);
}
