package com.gx.morgan.imageloaderlib.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * description：
 * <br>author：caowugao
 * <br>time：2018/3/2 17:32
 */
public class ViewUtil {
    private ViewUtil(){}
    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static void setBackground(View view, Bitmap bitmap) {
        setBackground(view, new BitmapDrawable(view.getResources(), bitmap));
    }
}
