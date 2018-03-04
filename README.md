# ImageLoader
一个轻量级的图片加载库，支持https，支持三级缓存

#### 使用场景：
sdk开发中常用到

#### 使用方法：
1.在applicaion中初始化  ImageLoader.init(Context context, String baseDir,DisplayOptions options);

  在application的onTerminate 方法中释放资源 ImageLoader.release();

2.加载图片:

ImageLoader.display(String imageUrl, ImageView imageView);

ImageLoader.display(String imageUrl, ImageView imageView, int width, int height);//加载指定宽高

ImageLoader.display(String imageUrl, ImageView imageView, OnDisplayListener callback);//添加回调

ImageLoader.display(String imageUrl, ImageView imageView, int width, int height, OnDisplayListener callback);//加载指定宽高和添加回调