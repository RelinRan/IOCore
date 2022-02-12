package androidx.io.core.core;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限
 */
public class Permission {

    private static final String TAG = Permission.class.getSimpleName();
    /**
     * 请求代码
     */
    public static final int REQUEST_CODE = 398;
    /**
     * 照相机权限
     */
    public final static String GROUP_CAMERA[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    /**
     * 视频录制权限
     */
    public final static String GROUP_RECORD[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    /**
     * 定位权限
     */
    public final static String GROUP_LOCATION[] = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    /**
     * 页面
     */
    private Activity activity;
    /**
     * 碎片
     */
    private Fragment fragment;
    /**
     * 权限请求监听
     */
    private OnRequestPermissionsListener listener;

    /**
     * 权限构造函数
     *
     * @param activity 页面
     * @param listener 权限请求监听
     */
    public Permission(Activity activity, OnRequestPermissionsListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    /**
     * 权限构造函数
     *
     * @param fragment 页面
     * @param listener 权限请求监听
     */
    public Permission(Fragment fragment, OnRequestPermissionsListener listener) {
        this.fragment = fragment;
        this.listener = listener;
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }
        return activity;
    }

    /**
     * 是否显示权限理由
     *
     * @param permission
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean shouldShowRequestPermissionRationale(String permission) {
        if (activity != null) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return fragment.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * 检查权限
     *
     * @param permissions 权限
     * @return 授权失败的权限
     */
    public List<String> checkDenied(String[] permissions) {
        List<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(getContext(), permissions[i]) == PackageManager.PERMISSION_DENIED) {
                denied.add(permissions[i]);
            }
        }

        return denied;
    }

    /**
     * 检查不可用
     *
     * @param permissions 权限
     * @return
     */
    public List<String> checkDisabled(String[] permissions) {
        List<String> disabled = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(permissions[i])) {
                    disabled.add(permissions[i]);
                }
            }
        }
        return disabled;
    }

    /**
     * 集合转数组
     *
     * @param permissions 权限
     * @return
     */
    public String[] toArray(List<String> permissions) {
        int size = permissions == null ? 0 : permissions.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = permissions.get(i);
        }
        return array;
    }

    /**
     * 数组转集合
     *
     * @param permissions 权限
     * @return
     */
    public List<String> toList(String[] permissions) {
        int size = permissions.length;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(permissions[i]);
        }
        return list;
    }

    /**
     * 请求权限
     *
     * @param permissions 权限数组
     * @param requestCode 请求代码
     */
    public void requestPermissions(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "requestPermissions permissions: " + toList(permissions).toString() + ", requestCode: " + requestCode);
            List<String> disabled = checkDisabled(permissions);
            if (disabled.size() > 0) {
                Log.i(TAG, "disabled: " + disabled.toString());
                if (listener != null) {
                    listener.onRequestPermissionRationale(permissions);
                }
            } else {
                List<String> denied = checkDenied(permissions);
                if (denied.size() > 0) {
                    Log.i(TAG, "denied: " + denied.toString());
                    activity.requestPermissions(toArray(denied), requestCode);
                } else {
                    if (listener != null) {
                        listener.onRequestPermissionsGranted(permissions);
                    }
                }
            }
        } else {
            if (listener != null) {
                listener.onRequestPermissionsGranted(permissions);
            }
        }
    }

    /**
     * 处理页面的权限请求
     *
     * @param requestCode  请求代码
     * @param permissions  权限
     * @param grantResults 获取的权限
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int permissionsSize = permissions.length, grantResultSize = grantResults.length;
        List<String> disabled = checkDisabled(permissions);
        Log.i(TAG, "onRequestPermissionsResult requestCode: " + requestCode + ", permissionsSize: " + permissionsSize + ",grantResultSize: " + grantResultSize);
        if (disabled.size() > 0) {
            Log.i(TAG, "disabled: " + disabled.toString());
            if (listener != null) {
                listener.onRequestPermissionRationale(toArray(disabled));
            }
        } else {
            List<String> denied = checkDenied(permissions);
            Log.i(TAG, "denied: " + denied.toString());
            if (denied.size() > 0) {
                if (listener != null) {
                    listener.onRequestPermissionsDenied(toArray(denied));
                }
            } else {
                if (listener != null) {
                    listener.onRequestPermissionsGranted(permissions);
                }
            }
        }
    }

    /**
     * 跳转设置页面
     */
    public void startSettingActivity() {
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            mIntent.setData(Uri.fromParts("package", getContext().getApplicationContext().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            mIntent.setAction(Intent.ACTION_VIEW);
            mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
            mIntent.putExtra("com.android.settings.ApplicationPkgName", getContext().getApplicationContext().getPackageName());
        }
        getContext().startActivity(mIntent);
    }

    /**
     * 权限操作监听
     */
    public interface OnRequestPermissionsListener {

        /**
         * 已全通过权限
         *
         * @param permissions
         */
        void onRequestPermissionsGranted(String[] permissions);

        /**
         * 已拒绝权限
         *
         * @param permissions
         */
        void onRequestPermissionsDenied(String[] permissions);

        /**
         * 已禁止权限
         *
         * @param permissions
         */
        void onRequestPermissionRationale(String[] permissions);

    }

}
