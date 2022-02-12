package androidx.io.core.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraProvider {

    private static final String TAG = CameraProvider.class.getSimpleName();
    /**
     * 相机后置-预览角度
     */
    public static int CAMERA_PREVIEW_BACK_DEGREES = 90;
    /**
     * 相机前置-预览角度
     */
    public static int CAMERA_PREVIEW_FRONT_DEGREES = 90;
    /**
     * 相机后置-拍照角度
     */
    public static int CAMERA_CAPTURE_BACK_DEGREES = 90;
    /**
     * 相机前置-拍照角度
     */
    public static int CAMERA_CAPTURE_FRONT_DEGREES = 270;

    /**
     * 切换闪光灯
     *
     * @param camera 摄像头
     */
    public static void toggleFlash(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        boolean isOff = parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF);
        Log.i(TAG, "toggleFlash isOff: " + isOff);
        parameters.setFlashMode(isOff ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    /**
     * 是否打开闪光灯
     *
     * @param camera
     * @return
     */
    public static boolean isFlashOff(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF);
    }

    /**
     * 获取角度
     *
     * @param cameraId 相机ID
     * @return
     */
    public static int getPreviewDegrees(int cameraId) {
        return cameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? CAMERA_PREVIEW_BACK_DEGREES : CAMERA_PREVIEW_FRONT_DEGREES;
    }

    /**
     * 获取拍照角度
     *
     * @param cameraId 相机ID
     * @return
     */
    public static int getCaptureDegrees(int cameraId) {
        return cameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? CAMERA_CAPTURE_BACK_DEGREES : CAMERA_CAPTURE_FRONT_DEGREES;
    }

    /**
     * 旋转图片
     *
     * @param data    图片数据
     * @param degrees 旋转角度
     * @return
     */
    public static Bitmap rotate(byte[] data, float degrees) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 处理拍照数据，获得Uri
     *
     * @param context 上下文
     * @param data    数据
     * @param camera  摄像头
     * @param degrees 旋转角度
     */
    public static Uri getTakenPictureUri(Context context, byte[] data, Camera camera, float degrees) {
        Bitmap target = rotate(data, degrees);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        target.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        data = bos.toByteArray();
        Uri output = UriProvider.tempImageUri(context);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(context.getContentResolver().openFileDescriptor(output, "rw").getFileDescriptor());
            os.write(data);
        } catch (FileNotFoundException e) {
            UriProvider.delete(context, output);
            e.printStackTrace();
        } catch (IOException e) {
            UriProvider.delete(context, output);
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                camera.stopPreview();
                camera.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    /**
     * 拷贝副本
     *
     * @param context 上下文
     * @param uri     文件
     * @return
     */
    public File copy(Context context, Uri uri) {
        return UriProvider.copy(context, uri, "Camera");
    }

    /**
     * 打开摄像头
     *
     * @param cameraId 摄像头ID{@link Camera.CameraInfo#CAMERA_FACING_BACK}
     * @param degrees  旋转角度
     * @param width    拍摄宽度
     * @param height   拍摄高度
     */
    public static Camera obtainCamera(int cameraId, int degrees, int width, int height) {
        Camera camera = Camera.open(cameraId);
        Camera.Parameters parameters = camera.getParameters();
        parameters.set("orientation", "portrait");
        Camera.Size size = findSupportedPreviewSize(camera, width, height);
        Log.i(TAG, "obtainCamera size: " + size.width + " * " + size.height);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(degrees);
        return camera;
    }

    /**
     * 开启预览
     *
     * @param camera 摄像头
     * @param holder 显示器
     */
    public static void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        camera.cancelAutoFocus();
    }

    /**
     * 找到支持的预览尺寸
     *
     * @param camera 摄像头
     * @param width  宽度
     * @param height 高度
     * @return
     */
    public static Camera.Size findSupportedPreviewSize(Camera camera, int width, int height) {
        float targetScale = width * 1.0F / height * 1.0F;
        Log.i(TAG, "targetScale: " + targetScale);
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        for (int i = 0; i < sizeList.size(); i++) {
            Camera.Size size = sizeList.get(i);
            float scale = size.width * 1.0F / size.height * 1.0F;
            Log.i(TAG, size.width + " * " + size.height + " ,scale: " + scale);
        }
        return getOptimalPreviewSize(sizeList, width, height);
    }

    /**
     * 获取合适的预览尺寸
     *
     * @param sizes  尺寸集合
     * @param width  宽度
     * @param height 高度
     * @return
     */
    private static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) width / height;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = height;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * 设置相机焦点
     *
     * @param camera
     */
    public static void focus(Camera camera) {
        if (camera != null) {
            try {
                camera.cancelAutoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Rect focusRect = new Rect(-1000, -1000, 1000, 1000);
            Rect meteringRect = new Rect(-1000, -1000, 1000, 1000);
            Camera.Parameters parameters = null;
            try {
                parameters = camera.getParameters();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (parameters != null) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> focus = new ArrayList<>();
                focus.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(focus);
                if (parameters.getMaxNumMeteringAreas() > 0) {
                    List<Camera.Area> metering = new ArrayList<>();
                    metering.add(new Camera.Area(meteringRect, 1000));
                    parameters.setMeteringAreas(metering);
                }
                try {
                    camera.setParameters(parameters);
                    camera.autoFocus((success, camera1) -> {

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
