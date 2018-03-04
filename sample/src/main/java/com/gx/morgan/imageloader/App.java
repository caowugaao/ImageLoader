package com.gx.morgan.imageloader;

import android.app.Application;

import com.gx.morgan.imageloaderlib.ImageLoader;
import com.gx.morgan.imageloaderlib.entity.DisplayOptions;

/**
 * description：
 * <br>author：caowugao
 * <br>time：2018/3/2 17:43
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DisplayOptions options=new DisplayOptions();
        options.placeholderResId=R.mipmap.ic_launcher;
        options.failResId=R.mipmap.ic_load_fail;
        ImageLoader.init(this,null,options);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ImageLoader.release();
    }
}
