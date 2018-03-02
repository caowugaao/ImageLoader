package com.gx.morgan.imageloaderlib.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.gx.morgan.imageloaderlib.diskcache.DiskLruCache;
import com.gx.morgan.imageloaderlib.interfaces.OnDisplayListener;
import com.gx.morgan.imageloaderlib.util.DiskLruCacheUtil;
import com.gx.morgan.imageloaderlib.util.Md5Util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * description：图片加载核心类
 * <br>author：caowugao
 * <br>time： 2017/09/14 10:38
 */

public class ImageWorker extends AbstractImageWorker implements ImageDownloadTask.OnImageDownloadListener {
    private static ImageWorker instance;
    private String mBasePathDir;
    private static final String TAG = ImageWorker.class.getSimpleName();
    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    private static final String FILE_PARENT = "images";
    private static final long MAX_CACHE_SIZE = 100 * 1024 * 1024;
    private DiskLruCache mDiskLruCache;
    private ExecutorService executorService;
    private static final int THREAD_NUM = 5;
    /**
     * hashKey=OnDisplayListener
     */
    private Map<String, OnDisplayListener> callbackMap = new LinkedHashMap<>();
    /**
     * hashKey=ImageDownloadTask
     */
    private Map<String, ImageDownloadTask> taskMap = new LinkedHashMap<>();

    private ImageWorker() {
    }

    public static ImageWorker getInstance() {
        if (null == instance) {
            synchronized (ImageWorker.class) {
                if (null == instance) {
                    instance = new ImageWorker();
                }
            }
        }
        return instance;
    }

    /**
     * basePathDir+images目录目录
     *
     * @param context
     * @param basePathDir 存放目录，为null则默认存放在context.getCacheDir()+images目录
     */
    private void init(Context context, String basePathDir, int cacheMemory) {
        if (null != mBasePathDir && !"".equals(mBasePathDir)) {
            return;
        }
        if (null == basePathDir || "".equals(basePathDir)) {
            mBasePathDir = context.getCacheDir().getAbsolutePath() + File.separator;
        } else {
            mBasePathDir = basePathDir;
            if (!mBasePathDir.endsWith(File.separator)) {
                mBasePathDir = mBasePathDir + File.separator;
            }
        }
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mDiskLruCache = DiskLruCacheUtil.getDiskLruCache(mBasePathDir + FILE_PARENT, context,
                MAX_CACHE_SIZE);
        executorService = Executors.newFixedThreadPool(THREAD_NUM);
    }

    private void execute(Runnable task) {
        executorService.execute(task);
    }

    @Override
    public void addToDisk(String url, Bitmap bitmap) {
        synchronized (mDiskLruCache) {
            DiskLruCacheUtil.saveImage(url, bitmap, mDiskLruCache);
        }
    }

    /**
     * 将图片加入LruCache
     *
     * @param url
     * @param bm
     */
    @Override
    public void addToMemory(String url, Bitmap bm) {
        if (null == getFromMemory(url)) {
            if (null != bm) {
                mLruCache.put(url, bm);
            }
        }
    }

    @Override
    public void removeInDisk(String url) {
        DiskLruCacheUtil.removeImage(url, mDiskLruCache);
    }

    /**
     * 在Application中初始化
     *
     * @param context
     * @param baseDir     为null则默认存放在context.getCacheDir()+images目录
     * @param cacheMemory 最大占用内存
     */
    public void initCacheDir(Context context, String baseDir, int cacheMemory) {
        init(context, baseDir, cacheMemory);
    }

    /**
     * 在Application中初始化，默认内存缓存为运行内存1/8
     *
     * @param context
     * @param baseDir 为null则默认存放在context.getCacheDir()+images目录
     */
    public void initCacheDir(Context context, String baseDir) {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int imageCacheMemory = maxMemory / 8;
        init(context, baseDir, imageCacheMemory);
    }

    @Override
    public void requestNetwork(String imageUrl, ImageView imageView, OnDisplayListener callback) {
        String key = Md5Util.hashKeyForDisk(imageUrl);
        ImageDownloadTask existTask = taskMap.get(key);
        if (null != existTask) {
            return;
        }

        callbackMap.put(key, callback);

        ImageDownloadTask newTask = new ImageDownloadTask(imageUrl, imageView, this);
        execute(newTask);
        taskMap.put(key, existTask);
    }

    @Override
    public void requestNetwork(final String imageUrl, ImageView imageView) {
        String key = Md5Util.hashKeyForDisk(imageUrl);
        ImageDownloadTask existTask = taskMap.get(key);
        if (null != existTask) {
            return;
        }

        ImageDownloadTask task = new ImageDownloadTask(imageUrl, imageView, this);
        execute(task);
        taskMap.put(key, existTask);
    }

    /**
     * 根据path在缓存中获取bitmap
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFromLruCache(String url) {
        return mLruCache.get(url);
    }

    @Override
    public Bitmap getFromDisk(String url, int width, int height) {
        return DiskLruCacheUtil.readImage(url, mDiskLruCache, width, height);
//        return DiskLruCacheUtil.readImage(url, mDiskLruCache);
    }

    @Override
    public void onImageRequestSuccess(String url, ImageView imageView, Bitmap bitmap) {
        addToMemory(url, bitmap);

        String key = Md5Util.hashKeyForDisk(url);
        OnDisplayListener callback = callbackMap.get(key);
        if (null != callback) {
            Bitmap transform = callback.onTransform(url, bitmap);
            if (null != imageView) {
                imageView.setImageBitmap(transform);
            }
            callback.onImageDisplaySuccess(url, imageView, transform);
            callbackMap.remove(key);
        } else {
            if (null != imageView) {
                imageView.setImageBitmap(bitmap);
            }
        }

        ImageDownloadTask task = taskMap.get(key);
        if (null != task) {
            taskMap.remove(key);
        }
    }

    @Override
    public void onImageRequestFail(String url, int code, String msg) {
        Log.e(TAG, "onImageRequestFail: imageUrl=" + url + ", code=" + code + ", msg=" + msg);

        String key = Md5Util.hashKeyForDisk(url);
        OnDisplayListener callback = callbackMap.get(key);
        if (null != callback) {
            callback.onImageDisplayFail(url, code, msg);
            callbackMap.remove(key);
        }

        ImageDownloadTask task = taskMap.get(key);
        if (null != task) {
            taskMap.remove(key);
        }
    }

    /**
     * 注意是在子线程
     *
     * @param url
     * @param bitmap
     */
    @Override
    public void onSaveToDisk(String url, Bitmap bitmap) {
        addToDisk(url, bitmap);
    }

    @Override
    public Bitmap getFromMemory(String url) {
        return getBitmapFromLruCache(url);
    }

    /**
     * 清空缓存数据
     */
    @Override
    public void clearInDisk() {
        DiskLruCacheUtil.clear(mDiskLruCache);
    }

    /**
     * 关闭DiskLruCache ，退出应用中调用
     */
    @Override
    public void release() {
        try {
            if (null != executorService) {
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                    executorService = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DiskLruCacheUtil.close(mDiskLruCache);
    }

   
    @Override
    public void justDownload(String url, ImageView imageView, ImageDownloadTask.OnImageDownloadListener listener) {
        ImageDownloadTask task = new ImageDownloadTask(url, imageView, listener);
        execute(task);
        
    }

}
