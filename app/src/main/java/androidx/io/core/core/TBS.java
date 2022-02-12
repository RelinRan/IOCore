package androidx.io.core.core;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.TbsVideo;

import java.io.File;
import java.util.HashMap;

/**
 * 腾讯TBS
 */
public class TBS implements QbSdk.PreInitCallback {

    public final static String TAG = TBS.class.getSimpleName();
    private static TBS instance;

    private TBS(Context context, String appId) {
        initSettings(null);
        QbSdk.initX5Environment(context, this);
        if (!TextUtils.isEmpty(appId)) {
            CrashReport.initCrashReport(context.getApplicationContext(), appId, false);
        }
    }

    /**
     * 初始化设置参数，在调用TBS初始化、创建WebView之前进行如下配置
     *
     * @param settings
     */
    public static void initSettings(HashMap settings) {
        if (settings == null) {
            settings = new HashMap();
            settings.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
            settings.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        }
        QbSdk.initTbsSettings(settings);
    }

    /**
     * 初始化TBS
     *
     * @param context 上下文对象
     * @param appId   腾讯Bug记录平台appId,如果不需要直接传NULL,官网：https://bugly.qq.com/v2/
     * @return
     */
    public static TBS initialize(Context context, String appId) {
        if (instance == null) {
            synchronized (TBS.class) {
                if (instance == null) {
                    instance = new TBS(context.getApplicationContext(), appId);
                }
            }
        }
        if (!QbSdk.isTbsCoreInited()) {
            QbSdk.initX5Environment(context, new TBS(context, appId));
        }
        return instance;
    }

    @Override
    public void onCoreInitFinished() {
        Log.i(TAG, "onCoreInitFinished");
    }

    @Override
    public void onViewInitFinished(boolean isX5Core) {
        Log.i(TAG, "onViewInitFinished isX5Core: " + isX5Core);
    }

    /**
     * 是否支持预览
     *
     * @param readerView 文件路径
     * @param path       文件路径
     * @return
     */
    public static boolean isSupport(TbsReaderView readerView, String path) {
        String suffix = path.substring(path.lastIndexOf(".") + 1);
        Log.i(TAG, "suffix: " + suffix);
        return readerView.preOpen(suffix, false);
    }

    /**
     * 打开文件
     *
     * @param context    上下文对象
     * @param readerView 预览View
     * @param file       文件
     * @return
     */
    public static void openFile(Context context, TbsReaderView readerView, File file) {
        String filePath = file.getAbsolutePath();
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", IOProvider.makeCacheDir(context, "TBS").getAbsolutePath());
        readerView.openFile(bundle);
    }

    /**
     * 打开视频
     *
     * @param context 上下文
     * @param file    文件
     */
    public static void openVideo(Context context, File file) {
        TbsVideo.openVideo(context, file.getAbsolutePath());
    }

}
