package androidx.io.core.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.io.core.R;
import androidx.io.core.core.CameraProvider;
import androidx.io.core.core.UriProvider;
import androidx.io.core.core.CameraOptions;
import androidx.io.core.video.VideoRecorder;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * 视频录制
 */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener, VideoRecorder.OnVideoTimerListener {

    public final String TAG = CameraActivity.class.getSimpleName();
    public static final String EXTRA_OPT = "camera_opt";
    public static final int REQUEST_CODE = 547;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceView surfaceView;
    private ImageView startView;
    private ImageView finishView;
    private LinearLayout finishGroup;
    private ImageView flashView;
    private ImageView switchView;
    private ImageView confirmView;
    private ImageView cancelView;
    private TextView durationView;
    private View v_right;

    private boolean capture;
    private CameraOptions options;
    private VideoRecorder recorder;

    /**
     * 跳转到视频录制页面
     *
     * @param activity 页面
     * @param options  参数
     */
    public static void start(AppCompatActivity activity, CameraOptions options) {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra(EXTRA_OPT, options);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 跳转到视频录制页面
     *
     * @param fragment 页面
     * @param options  参数
     */
    public static void start(Fragment fragment, CameraOptions options) {
        Intent intent = new Intent(fragment.getContext(), CameraActivity.class);
        intent.putExtra(EXTRA_OPT, options);
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 获取视频数据
     *
     * @param data onActivityResult返回的data
     * @return
     */
    public static Uri getData(Intent data) {
        return data.getData();
    }

    /**
     * 获取副本文件
     *
     * @param context 上下文
     * @param data    onActivityResult返回的data
     * @return
     */
    public static File getFile(Context context, Intent data) {
        Uri uri = data.getData();
        File dir = new File(context.getExternalCacheDir(), "Camera");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = UriProvider.queryDisplayName(context, uri);
        File file = new File(dir, filename);
        UriProvider.copy(context, uri, file);
        return file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.io_core_video_record);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initialize();
    }

    /**
     * 是否拍照
     *
     * @return
     */
    public boolean isCapture() {
        return capture;
    }

    /**
     * 初始化
     */
    private void initialize() {
        findViewsById();
        Serializable serializable = getIntent().getSerializableExtra(EXTRA_OPT);
        if (serializable == null) {
            recorder = new VideoRecorder(this, surfaceView);
        } else {
            options = (CameraOptions) serializable;
            capture = options.isCapture();
            recorder = new VideoRecorder(this, surfaceView);
            recorder.setOutput(options.getOutput());
            recorder.setWidth(options.getWidth());
            recorder.setHeight(options.getHeight());
            recorder.setVideoEncoder(options.getVideoEncoder());
            recorder.setCameraId(options.getCameraId());
            recorder.setDuration(options.getDuration());
            Log.i(TAG, "options " + options.toString());
        }
        //拍照按钮
        startView.setImageResource(isCapture() ? R.mipmap.io_core_capture : R.mipmap.io_core_record_start);
        //时间
        durationView.setVisibility(isCapture() ? View.GONE : View.VISIBLE);
        //计时器监听
        recorder.setOnVideoTimerListener(this);
        //开启预览
        recorder.startPreview();
    }

    /**
     * 找到控件View
     */
    private void findViewsById() {
        surfaceView = findViewById(R.id.sv_camera);
        startView = findViewById(R.id.iv_start);
        flashView = findViewById(R.id.iv_flash);
        switchView = findViewById(R.id.iv_camera);
        finishView = findViewById(R.id.iv_finish);
        finishGroup = findViewById(R.id.ll_finish);
        durationView = findViewById(R.id.tv_duration);
        confirmView = findViewById(R.id.iv_confirm);
        cancelView = findViewById(R.id.iv_cancel);
        v_right = findViewById(R.id.v_right);
        //点击事件
        finishView.setOnClickListener(this);
        cancelView.setOnClickListener(this);
        confirmView.setOnClickListener(this);
        switchView.setOnClickListener(this);
        flashView.setOnClickListener(this);
        startView.setOnClickListener(this);
        //设置显示隐藏
        finishGroup.setVisibility(View.VISIBLE);
        cancelView.setVisibility(View.GONE);
        confirmView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_flash) {
            Camera camera = recorder.getCamera();
            CameraProvider.toggleFlash(camera);
            flashView.setImageResource(CameraProvider.isFlashOff(camera) ? R.mipmap.io_core_flash_on : R.mipmap.io_core_flash_off);
        }
        if (id == R.id.iv_camera) {
            cameraId = recorder.isFacingBack() ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            recorder.switchCamera(cameraId);
        }
        if (id == R.id.iv_cancel || id == R.id.iv_finish) {
            recorder.delete();
            finish();
        }
        if (id == R.id.iv_start) {
            if (isCapture()) {
                takePicture(this, CameraProvider.getCaptureDegrees(cameraId));
            } else {
                toggleRecord(!recorder.isStart());
            }
        }
        if (id == R.id.iv_confirm) {
            setResult(recorder.getOutput());
        }
    }

    /**
     * 设置结果
     *
     * @param uri 文件
     */
    protected void setResult(Uri uri) {
        Intent intent = new Intent();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.setData(uri);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 拍照
     *
     * @param context 上下文
     * @param rotate  旋转角度
     */
    protected void takePicture(Context context, int rotate) {
        recorder.getCamera().takePicture(null, null, (data, camera) -> {
            setResult(CameraProvider.getTakenPictureUri(context, data, camera, rotate));
        });
    }

    /**
     * 切换录制
     *
     * @param start 是否开始
     */
    protected void toggleRecord(boolean start) {
        if (start) {
            flashView.setEnabled(true);
            switchView.setEnabled(false);
            flashView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);
            startView.setImageResource(R.mipmap.io_core_record_stop);
            if (recorder != null) {
                recorder.start();
            }
        } else {
            flashView.setEnabled(true);
            startView.setEnabled(true);
            switchView.setEnabled(true);
            flashView.setVisibility(View.VISIBLE);
            startView.setVisibility(View.INVISIBLE);
            v_right.setVisibility(View.INVISIBLE);
            finishGroup.setVisibility(View.VISIBLE);
            cancelView.setVisibility(View.VISIBLE);
            confirmView.setVisibility(View.VISIBLE);
            startView.setImageResource(R.mipmap.io_core_record_start);
            if (recorder != null) {
                recorder.stop();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recorder.release();
    }

    @Override
    public void onVideoTimer(long millis) {
        showVideoTime(millis, durationView);
        int dot = millis % 2 == 0 ? R.drawable.io_core_record_red_dot : R.drawable.io_core_record_white_dot;
        durationView.setCompoundDrawablesWithIntrinsicBounds(dot, 0, 0, 0);
    }

    @Override
    public void onVideoTimerFinish() {
        startView.performClick();
    }

    /**
     * 显示视频时间
     *
     * @param millis 时间
     * @param tvShow 控件
     */
    private void showVideoTime(long millis, TextView tvShow) {
        DecimalFormat format = new DecimalFormat("00");
        long second = millis / 1000;
        long hour = second / 60 / 60;
        String timeText = "";
        if (hour > 0) {
            long videoMinutes = (second - hour * 3600) / 60;
            long videoSecond = second % 60;
            timeText = format.format(hour) + ":" + format.format(videoMinutes) + ":" + format.format(videoSecond);
        } else {
            long videoSecond = second % 60;
            long videoMinutes = second / 60;
            timeText = format.format(videoMinutes) + ":" + format.format(videoSecond);
        }
        tvShow.setText(timeText);
    }

}
