package androidx.io.core.core;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBar {

    public static final String TAG = StatusBar.class.getSimpleName();

    /**
     * 默认透明度
     */
    public static final int STATUS_BAR_ALPHA = 0;
    /**
     * 状态栏id
     */
    private static final int STATUS_BAR_ID = 76259;


    /**
     * 显示状态栏
     *
     * @param activity 页面
     */
    public static void show(AppCompatActivity activity) {
        if (activity == null) {
            return;
        }
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 隐藏状态栏
     *
     * @param activity 页面
     */
    public static void hide(AppCompatActivity activity) {
        if (activity == null) {
            return;
        }
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 获取高度
     *
     * @param context 上下文对象
     * @return
     */
    public static int height(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static void immerse(AppCompatActivity activity){
        setTransparent(activity);
    }

    //===========================================[状态栏颜色]=============================================

    /**
     * 设置状态栏颜色
     *
     * @param activity 页面
     * @param color    状态栏颜色值
     */
    public static void setColor(AppCompatActivity activity, @ColorInt int color) {
        setColor(activity, color, STATUS_BAR_ALPHA);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity 页面
     * @param color    颜色值
     * @param alpha    透明度
     */
    public static void setColor(AppCompatActivity activity, @ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(createColor(color, alpha));
            setContentTopPadding(activity, color == Color.TRANSPARENT ? 0 : height(activity));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            addSameHeightView(activity, color, alpha);
            setFitsSystemWindowsClipPadding(activity);
        }

    }

    /**
     * 设置内容顶部间距
     *
     * @param activity 页面
     * @param padding  间距
     */
    public static void setContentTopPadding(AppCompatActivity activity, int padding) {
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        contentView.setPadding(0, padding, 0, 0);
    }

    /**
     * 重置之前设置
     *
     * @param activity 页面
     */
    public static void reset(AppCompatActivity activity) {
        removeSameHeightView(activity);
        setContentTopPadding(activity, 0);
    }

    //===========================================[文字颜色]=============================================

    /**
     * 设置字体
     *
     * @param activity
     * @param dark     是否是黑色
     */
    public static void setTextColor(AppCompatActivity activity, boolean dark) {
        setMeiZuTextColor(activity, dark);
        setXiaoMiTextColor(activity, dark);
        int visibility = dark ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN : View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (activity != null) {
            activity.getWindow().getDecorView().setSystemUiVisibility(visibility);
        }
    }

    /**
     * 修改 MIUI V6  以上状态栏颜色
     */
    private static void setXiaoMiTextColor(@NonNull AppCompatActivity activity, boolean dark) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), dark ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            Log.i(TAG, "Failed to set the text color of the Xiaomi status bar, the device is not a Xiaomi phone.");
        }
    }

    /**
     * 修改魅族状态栏字体颜色 Flyme 4.0
     */
    private static void setMeiZuTextColor(@NonNull AppCompatActivity activity, boolean darkIcon) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkIcon) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
        } catch (Exception e) {
            Log.i(TAG, "Failed to set the text color of Meizu status bar, the device is not a Meizu phone");
        }
    }


    //===========================================[背景透明度|颜色]=============================================

    /**
     * 设置半透明
     *
     * @param activity 页面
     */
    public static void setTranslucent(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置全透明
     *
     * @param activity
     */
    public static void setTransparent(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置根布局参数
     */
    public static void setFitsSystemWindowsClipPadding(AppCompatActivity activity) {
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        for (int i = 0, count = contentView.getChildCount(); i < count; i++) {
            View childView = contentView.getChildAt(i);
            if (childView instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) childView;
                viewGroup.setFitsSystemWindows(true);
                viewGroup.setClipToPadding(true);
            }
        }
    }

    //===========================================[等高View]=============================================


    /***
     * 设置等高View可见性
     * @param activity 页面
     * @param visibility 可见性
     */
    public static void setSameHeightViewVisibility(AppCompatActivity activity, int visibility) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View statusBarView = decorView.findViewById(STATUS_BAR_ID);
        if (statusBarView != null) {
            statusBarView.setVisibility(visibility);
        }
    }

    /**
     * 获取等高View
     *
     * @param activity 页面
     * @return
     */
    public static View getSameHeightView(AppCompatActivity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        return decorView.findViewById(STATUS_BAR_ID);
    }

    /**
     * 添加等高矩形条
     *
     * @param activity 页面
     * @param color    颜色
     * @param alpha    透明值
     */
    public static void addSameHeightView(AppCompatActivity activity, @ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View statusBarView = decorView.findViewById(STATUS_BAR_ID);
        if (statusBarView != null) {
            if (statusBarView.getVisibility() == View.GONE) {
                statusBarView.setVisibility(View.VISIBLE);
            }
            statusBarView.setBackgroundColor(createColor(color, alpha));
        } else {
            decorView.addView(createSameHeightView(activity, color));
        }
    }

    /**
     * 移除等高View
     *
     * @param activity 页面
     */
    public static void removeSameHeightView(AppCompatActivity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View sameHeightView = decorView.findViewById(STATUS_BAR_ID);
        if (sameHeightView != null) {
            decorView.removeView(sameHeightView);
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setPadding(0, 0, 0, 0);
        }
    }

    /**
     * 创建等高矩形条
     *
     * @param activity 页面
     * @param color    颜色
     * @return
     */
    public static View createSameHeightView(AppCompatActivity activity, @ColorInt int color) {
        return createSameHeightView(activity, color, 0);
    }

    /**
     * 创建等高矩形条
     *
     * @param activity 页面
     * @param color    颜色
     * @param alpha    透明值
     * @return
     */
    public static View createSameHeightView(AppCompatActivity activity, @ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        View statusBarView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(createColor(color, alpha));
        statusBarView.setId(STATUS_BAR_ID);
        return statusBarView;
    }

    /**
     * 创建颜色
     *
     * @param color color
     * @param alpha 透明值
     * @return 颜色值
     */
    public static int createColor(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

}
