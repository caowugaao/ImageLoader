package com.gx.morgan.imageloaderlib.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gx.morgan.imageloaderlib.diskcache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * description：
 * <br>author：caowugao
 * <br>time： 2017/09/14 11:34
 */

public class DiskLruCacheUtil {
    private DiskLruCacheUtil() {
    }

    private static final String DEFAULT_UNIQUE_NAME = "bitmap";
    /**
     * 默认存放大小100M
     */
    private static final long DEFAULT_MAX_SIZE = 100 * 1024 * 1024;

    /**
     * @param cacheDirString 若传null，则默认放在/data/data/application package/cache/bitmap/
     * @param context
     * @param maxSize
     * @return
     */
    public static DiskLruCache getDiskLruCache(String cacheDirString, Context context, long maxSize) {
        File cacheDir = null;
        if (null == cacheDirString || "".equals(cacheDirString)) {
            cacheDir = new File(context.getCacheDir().getAbsolutePath() + File.separator + DEFAULT_UNIQUE_NAME);
        } else {
            cacheDir = new File(cacheDirString);
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            // 创建一个DiskLruCache的实例
            return DiskLruCache.open(cacheDir, getAppVersion(context), 1, maxSize <= 0 ?
                    DEFAULT_MAX_SIZE : maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前应用版本号
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }


    /**
     * 保存
     *
     * @param imageUrl
     * @param imageIs       图片的InputStream
     * @param isNeedCloseIs 是否需要关闭imageIs
     * @param diskLruCache
     */
    public static void saveImage(String imageUrl, InputStream imageIs, boolean isNeedCloseIs, DiskLruCache
            diskLruCache) {
        if (null == diskLruCache || null == imageIs) {
            return;
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        DiskLruCache.Editor editor = null;
        try {
            String key = Md5Util.hashKeyForDisk(imageUrl);
            editor = diskLruCache.edit(key);
            if (null != editor) {
                OutputStream outputStream = editor.newOutputStream(0);
//                bis = new BufferedInputStream(imageIs, 8 * 1024);
//                bos = new BufferedOutputStream(outputStream, 8 * 1024);
                bis = new BufferedInputStream(imageIs);
                bos = new BufferedOutputStream(outputStream);

                byte[] buff = new byte[4 * 1024];
                int len;
                while ((len = bis.read(buff)) > 0) {
                    bos.write(buff, 0, len);
                }
                bos.flush();

                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (null != editor) {
                    editor.abort();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } finally {
            try {
                close(bos);
                close(bis);
                if (isNeedCloseIs) {
                    close(imageIs);
                }
                if (null != diskLruCache) {
                    diskLruCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 保存
     *
     * @param imageUrl
     * @param bitmap
     * @param diskLruCache
     */
    public static void saveImage(String imageUrl, Bitmap bitmap, DiskLruCache diskLruCache) {
        if (null == diskLruCache || null == bitmap) {
            return;
        }
        BufferedOutputStream bos = null;
        DiskLruCache.Editor editor = null;
        try {
            String key = Md5Util.hashKeyForDisk(imageUrl);
            editor = diskLruCache.edit(key);
            if (null != editor) {
                OutputStream outputStream = editor.newOutputStream(0);
                bos = new BufferedOutputStream(outputStream, 8 * 1024);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();

            try {
                if (null != editor) {
                    editor.abort();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                close(bos);

                if (null != diskLruCache) {
                    diskLruCache.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void close(Closeable closeable) throws IOException {
        if (null != closeable) {
            closeable.close();
        }
    }

    /**
     * 读取图片
     *
     * @param imageUrl
     * @param diskLruCache
     * @return
     */
    public static Bitmap readImage(String imageUrl, DiskLruCache diskLruCache) {
        if (null == diskLruCache) {
            return null;
        }
        String key = Md5Util.hashKeyForDisk(imageUrl);
        try {
            DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
            if (null != snapShot) {
                InputStream is = snapShot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从FileDescriptor中获取合适大小的bitmap，避免OOM
     *
     * @param imageUrl
     * @param diskLruCache
     * @param requestWidth
     * @param requestHeight
     * @return
     */
    public static Bitmap readImage(String imageUrl, DiskLruCache diskLruCache, int requestWidth, int requestHeight) {
        if (null == diskLruCache) {
            return null;
        }

        String key = Md5Util.hashKeyForDisk(imageUrl);
        try {
            DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
            if (null != snapShot) {
                InputStream is = snapShot.getInputStream(0);
                FileInputStream fis = (FileInputStream) is;
                FileDescriptor fd = fis.getFD();

                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, requestWidth, requestHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFileDescriptor(fd, null, options);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqWidth == 0 || reqHeight == 0) {
            return 1;
        }

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
//        Log.d(TAG, "origin, w= " + width + " h=" + height);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

//        Log.d(TAG, "sampleSize:" + inSampleSize);
        return inSampleSize;
    }

    /**
     * 获取缓存的大小
     *
     * @param diskLruCache
     * @return
     */
    public static long getSize(DiskLruCache diskLruCache) {
        return null == diskLruCache ? 0 : diskLruCache.size();
    }

    /**
     * 移除image
     *
     * @param imageUrl
     * @param diskLruCache
     */
    public static void removeImage(String imageUrl, DiskLruCache diskLruCache) {
        if (null == diskLruCache) {
            return;
        }

        try {
            String key = Md5Util.hashKeyForDisk(imageUrl);
            diskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空缓存数据
     *
     * @param diskLruCache
     */
    public static void clear(DiskLruCache diskLruCache) {
        if (null == diskLruCache) {
            return;
        }
        try {
            diskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭DiskLruCache ，通常放在Activity.onDestroy()中，或者退出应用
     *
     * @param diskLruCache
     */
    public static void close(DiskLruCache diskLruCache) {
        if (null == diskLruCache) {
            return;
        }
        try {
            if (!diskLruCache.isClosed()) {
                diskLruCache.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
