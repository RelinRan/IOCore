# IOCore
Android文件操作工具，此工具采用沙盒安全模式，不要设置传统存储模式。
在Android11以后强制采用沙河模式，不再支持传统存储模式，采用java编写，
因为考虑kotlin和java语言都能调用，kotlin工具只能kotlin项目能用。
1.内含腾讯Bugly、TBS、ISOParser
2.系统+自定义相机拍照、录像
3.系统+自定义媒体文件选择，音频文件专辑图片兼容高版本。
4.文件打开预览
5.文件下载
6.数据(DataStore - SharedPreferences)
7.内含三方PhotoView
8.自定义进度圆圈CircleProgress控件
9.本地异常记录
# Maven
1.build.grade
```
allprojects {
    repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2./app/build.grade
```
dependencies {
	implementation 'com.github.RelinRan:IOCore:2022.2.20.1'
}
```
# 初始化
初始化腾讯TBS、Bugly(appId是腾讯平台ID)，如果不用Bugly设置appId为空。
```
TBS.initialize(applicationContext,appId);
```
配置权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
```
配置res/xml/path.xml
```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <root-path
        name="root"
        path="/storage/emulated/0" />
    <files-path
        name="files"
        path="/storage/emulated/0/Android/data/${applicationId}/files" />
    <cache-path
        name="cache"
        path="/storage/emulated/0/Android/data/${applicationId}/cache" />
    <external-path
        name="external"
        path="/storage/emulated/0/Android/data/${applicationId}/external" />
    <external-files-path
        name="Capture"
        path="/storage/emulated/0/Android/data/${applicationId}/files/Capture" />
    <external-cache-path
        name="Pick"
        path="/storage/emulated/0/Android/data/${applicationId}/files/Pick" />
    <external-cache-path
        name="TBS"
        path="/storage/emulated/0/Android/data/${applicationId}/files/TBS" />
</paths>
```
配置AndroidManifest.xml
```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileProvider"
    android:exported="false"
    android:grantUriPermissions="true"
    android:permission="android.permission.MANAGE_DOCUMENTS">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/path" />
    <intent-filter>
        <action android:name="android.content.action.DOCUMENTS_PROVIDER" />
    </intent-filter>
</provider>
<!--拍照、录像-->
<activity android:name="androidx.io.core.app.CameraActivity" />
<!--媒体选择-->
<activity android:name="androidx.io.core.app.MediaActivity" />
<!--文件预览-->
<activity android:name="androidx.io.core.app.TBSActivity" />
<!--TBS视频播放-->
<activity
    android:name="com.tencent.smtt.sdk.VideoActivity"
    android:alwaysRetainTaskState="true"
    android:configChanges="orientation|screenSize|keyboardHidden"
    android:exported="false"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="com.tencent.smtt.tbs.video.PLAY" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
<service
    android:name="com.tencent.smtt.export.external.DexClassLoaderProviderService"
    android:label="dexopt"
    android:process=":dexopt" />
```

# 预览文件
## 本地文件
```
TBSActivity.start(activity,file);
```
## 网络文件
override：是否覆盖原下载文件
```
TBSActivity.start(activity,"http://xxx.xxx.xxx.png",override);
```
# 下载文件
```
Downloader downloader = new Downloader(this, url);
downloader.setOverride(false);
downloader.setOnDownloadListener(new OnDownloadListener() {
    @Override
    public void onDownloading(long total, long progress) {
        
    }

    @Override
    public void onDownloadCompleted(File file) {

    }

    @Override
    public void onDownloadFailed(Exception e) {

    }
});
downloader.start();
```
# 选择文件
## 系统选择
```
MediaProvider mediaProvider = new MediaProvider(activity);
mediaProvider.pick("image/*");//视频：video/* 图片：image/*

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mediaProvider.onActivityResult(requestCode, resultCode, data, (request_code, file) -> {
         //TODO:file为结果 
    });
}
```
## 定义选择
```
MediaActivity.start(this, Media.IMAGE, false, true, MediaActivity.UNLIMITED)

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == MediaActivity.REQUEST_CODE) {
        ArrayList<Media> medias = MediaActivity.getResult(data);
    }
}
```
# 拍照
## 系统拍照
```
MediaProvider mediaProvider = new MediaProvider(activity);
mediaProvider.capture();

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mediaProvider.onActivityResult(requestCode, resultCode, data, (request_code, file) -> {
        //TODO:file为结果 
    });
}
```
## 定义拍照
```
CameraActivity.start(this,new CameraOptions(true));

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == CameraActivity.REQUEST_CODE) {
        File file = CameraActivity.getFile(this,data);
    }
}
```
# 录像
## 系统录像
```
VideoProvider videoProvider = new VideoProvider(this);
videoProvider.record();

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    videoProvider.onActivityResult(requestCode, resultCode, data, (request_code, file) -> {
         //TODO:file为结果 
    });
}
```
## 定义录像
```
CameraActivity.start(this,new CameraOptions(false));

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == CameraActivity.REQUEST_CODE) {
        File file = CameraActivity.getFile(this,data);
    }
}
```
# 图片
## 是否是图片
```
ImageProvider.isBitmap(File file);
```
## 是否可压缩
```
ImageProvider.isCompressible(String path);
```
## 创建文件名
```
ImageProvider.createName(prefix,suffix);
```
## 创建文件
```
ImageProvider.createCacheFile(context,dirName,suffix);
```
## 缓存文件夹
name:ImageProvider.DIRECTORY_PICTURES
```
File dir = ImageProvider.getCacheDir(Context context, String name);
```
## 清除文件
```
ImageProvider.clear(context,ImageProvider.DIRECTORY_COMPRESS);
ImageProvider.clear(context,ImageProvider.DIRECTORY_CROP);
ImageProvider.clear(context,ImageProvider.DIRECTORY_PICTURES);
```
## 纠正角度
```
ImageProvider.correct(path);
```
## 压缩文件
```
ImageProvider.compress(Bitmap bitmap, long max, Bitmap.CompressFormat format);
```
## 转文件
```
ImageProvider.toFile(Bitmap bitmap, String path);
```
## 按大小解析图片
```
ImageProvider.decodePath(String path, int width, int height);
```
## Uri转Bitmap
```
Bitmap bitmap = ImageProvider.decodeUri(Context context, Uri uri);
```
## 图片缩放参数
```
BitmapFactory.Options options = ImageProvider.inSampleSize(String path, int width, int height);
```
## 文件转base64
encode:是否URLEncoder
```
String base64 = ImageProvider.encodeBase64(File file, boolean encode);
```
## Base64转File
encode:是否URLEncoder
```
File file = ImageProvider.decodeBase64(String base64, String path, boolean decode);
```
## 图片缩略图
encode:是否URLEncoder
```
Bitmap bitmap = ImageProvider.createImageThumbnail(String path, int width, int height);
```
# 视频
## 创建缓存文件
```
VideoProvider.createFile(Context context, String dirName, String fileName);
```
## 拷贝文件
```
VideoProvider.copy(Uri from, File to);
```
## 删除文件
```
VideoProvider.delete(Uri uri);
```
## 查询名称
```
String displayName = VideoProvider.queryDisplayName(Uri uri);
```
## 缩略图
```
Bitmap bitmap = VideoProvider.loadThumbnail(Context context, Uri uri, int width, int height);
```
## 创建视频缩略图
```
Bitmap bitmap = VideoProvider.createVideoThumbnail(String path, int width, int height);
```
## 查询ID
```
long id = VideoProvider.queryId(Uri uri);
```
## Uri转File
```
File file = VideoProvider.transfer(Context context, Uri uri, String fileName);
```
## 提炼宽高
```
int[] wh = VideoProvider.extractSize(Context context, File file);
```
## 提炼时长
```
long duration = VideoProvider.extractDuration(Context context, Uri uri);
```
## 查询视频集合
```
List<Video> list = VideoProvider.query(Context context, Uri uri, String selection, String[] selectionArgs, String sortOrder);
```
## 压缩视频
```
VideoCompress compress = new VideoCompress(srcPath, desPath, VideoCompress.COMPRESS_QUALITY_MEDIUM);
compress.setOnVideoCompressListener(new OnVideoCompressListener() {
    @Override
    public void onVideoCompressProgress(float percent) {
        
    }
});
compress.start();
```
# 数据
## 添加
```
DataStore.put(context,key,value);
```
## 获取
```
DataStore.getString(context,key,defValue)
```
# 文件
## 获取外部缓存目录
```
IOProvider.getCacheDir(Context context);
```
## 获取应用外部文件夹
```
IOProvider.getFilesDir(Context context, String name);
```
## 创建新文件夹
```
IOProvider.makeFilesDir(Context context, String name);
```
## 创建新缓存文件夹
```
IOProvider.makeCacheDir(Context context, String name);
```
## 创建文件
```
IOProvider.createFile(Context context, String dirName, String fileName);
```
## 复制文件
```
IOProvider.copy(File from, File to);
```
## 删除文件
```
IOProvider.deleteDir(File file);
```
## 文件大小
```
IOProvider.length(file,IOProvider.UNIT_KB);
```
## 文件大小名称
```
IOProvider.lengthName(File file);
```
## 获取文件后缀
```
IOProvider.getSuffix(String path);
```
## 获取文件类型
```
IOProvider.getMimeType(String path);
```
## 获取Assets文件内容
```
IOProvider.readAssets(Context context, String fileName);
```
## 读取文件
```
IOProvider.read(File file);
```
## 写入文件内容
```
IOProvider.write(Context context, String fileName, String content);
```
## 文件流转File
```
IOProvider.decodeInputStream(InputStream inputStream, String path);
```
## File转Bytes
```
byte[] bytes = IOProvider.decodeFile(File file);
```
## Bytes转File
```
File file = IOProvider.decodeBytes(byte[] bytes, String path);
```
## 通过文件名获取资源id
例子：getResId("icon", R.drawable.class);
```
int resId = IOProvider.findResId(String variableName, Class<?> cls);
```
# Bug
异常捕捉。注意：使用这个类需要提前申请文件写入、读取权限，在Android 6.0需要动态申请权限。
```
Bug.Builder builder = new Bug.Builder(this);
builder.listener(new OnBugListener() {
    @Override
    public void onBug(File file, String bug) {

    }
});
builder.build();
```
# 圆圈进度
## 布局
```
<androidx.io.core.widget.CircleProgress
    android:layout_width="120dp"
    android:layout_height="120dp"
    app:circleBackgroundColor="#CAC3C3"
    app:circleProgressColor="#A035C2"
    app:circleStrokeWidth="10dp"
    android:max="100"
    android:textSize="16sp"
    android:textColor="#A035C2"
    android:progress="50"/>
```
## 进度颜色
```
setProgressColor(int progressColor);
```
## 进度背景颜色
```
setProgressBackgroundColor(int backgroundColor);
```
## 最大值
```
setMax(long max);
```
## 进度值
```
setProgress(long progress);
```
## 线条宽度
```
setStrokeWidth(int strokeWidth);
```



