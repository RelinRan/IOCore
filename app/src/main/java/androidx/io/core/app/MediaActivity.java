package androidx.io.core.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.io.core.R;
import androidx.io.core.media.Media;
import androidx.io.core.media.MediaProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.io.core.core.StatusBar;
import androidx.io.core.media.MediaAdapter;
import androidx.io.core.media.MediaExecutor;
import androidx.io.core.media.OnMediaItemClickListener;
import androidx.io.core.media.PopMenu;
import androidx.io.core.media.PopMenuWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 文件
 */
public class MediaActivity extends AppCompatActivity implements View.OnClickListener, OnMediaItemClickListener, TextWatcher {

    public final static int REQUEST_CODE = 5712;
    public final static int UNLIMITED = -1;
    protected String[] MENU_NAMES = {"手机媒体", "手机图片", "手机视频", "手机音频"};
    protected int[] MENU_ICONS = {
            R.mipmap.io_core_card_icon,
            R.mipmap.io_core_image_icon,
            R.mipmap.io_core_move_icon,
            R.mipmap.io_core_audio_icon
    };

    protected LinearLayout group_navigation;
    protected LinearLayout group_down;
    protected ImageView iv_close;
    protected TextView tv_name;
    protected ImageView iv_search;
    protected ImageView iv_down;
    protected TextView tv_confirm;
    protected LinearLayout group_search;
    protected EditText et_search;
    protected ImageView iv_cancel;
    protected RecyclerView rv_document;
    protected View v_cover;

    //选择类型
    protected int type = Media.DOCUMENT;
    //是否可切换类型
    protected boolean toggle = true;
    //最多选择几项，-1: 无限制
    protected int max = 5;
    //是可以多选
    protected boolean multiple = false;
    protected List<Media> files;
    protected List<Media> images;
    protected List<Media> videos;
    protected List<Media> audios;
    protected List<Media> checked;
    protected Stack<List<Media>> stack;
    protected MediaAdapter adapter;

    protected PopMenu popMenu;
    protected PopMenuWindow menuWindow;

    /**
     * 跳转文件选择页面
     *
     * @param activity 页面
     * @param type     类型{@link Media#DOCUMENT} or {@link Media#VIDEO} or {@link Media#VIDEO} OR {@link Media#AUDIO}
     * @param toggle   是否可切换类型
     * @param multiple 是否可多选
     * @param max      最多选择个数, -1:无上限
     */
    public static void start(AppCompatActivity activity, int type, boolean toggle, boolean multiple, int max) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("toggle", toggle);
        intent.putExtra("multiple", multiple);
        intent.putExtra("max", max);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 跳转文件选择页面
     *
     * @param fragment 页面
     * @param type     类型{@link Media#DOCUMENT} or {@link Media#VIDEO} or {@link Media#VIDEO} OR {@link Media#AUDIO}
     * @param toggle   是否可切换类型
     * @param multiple 是否可多选
     * @param max      最多选择个数, -1:无上限
     */
    public static void start(Fragment fragment, int type, boolean toggle, boolean multiple, int max) {
        Intent intent = new Intent(fragment.getContext(), MediaActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("toggle", toggle);
        intent.putExtra("multiple", multiple);
        intent.putExtra("max", max);
        fragment.startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 获取结果
     *
     * @param data onActivityResult返回data
     * @return
     */
    public static ArrayList<Media> getResult(Intent data) {
        return (ArrayList<Media>) data.getSerializableExtra("result");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.io_core_media);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        StatusBar.setTranslucent(this);
        StatusBar.setTextColor(this, false);
        //=================[Intent]=================
        type = getIntent().getIntExtra("type", Media.DOCUMENT);
        toggle = getIntent().getBooleanExtra("toggle", false);
        multiple = getIntent().getBooleanExtra("multiple", false);
        max = getIntent().getIntExtra("max", -1);
        //=================[View]=================
        group_navigation = findViewById(R.id.group_navigation);
        group_down = findViewById(R.id.group_down);
        iv_close = findViewById(R.id.iv_close);
        tv_name = findViewById(R.id.tv_name);
        iv_down = findViewById(R.id.iv_down);
        iv_search = findViewById(R.id.iv_search);
        tv_confirm = findViewById(R.id.tv_confirm);
        group_search = findViewById(R.id.group_search);
        et_search = findViewById(R.id.et_search);
        iv_cancel = findViewById(R.id.iv_cancel);
        rv_document = findViewById(R.id.rv_document);
        v_cover = findViewById(R.id.v_cover);
        //=================[监听]=================
        group_down.setOnClickListener(this);
        iv_close.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        v_cover.setOnClickListener(this);
        //=================[列表]=================
        adapter = new MediaAdapter(this);
        adapter.setMultiple(multiple);
        adapter.setOnMediaItemClickListener(this);
        rv_document.setLayoutManager(new LinearLayoutManager(this));
        rv_document.setAdapter(adapter);
        //=================[数据]=================
        stack = new Stack<>();
        checked = new ArrayList<>();
        files = MediaExecutor.getExecutor().getFiles();
        images = MediaExecutor.getExecutor().getImages();
        videos = MediaExecutor.getExecutor().getVideos();
        audios = MediaExecutor.getExecutor().getAudios();
        adapter.setData(files);
        //=================[弹出菜单]=================
        menuWindow = new PopMenuWindow(this);
        List<PopMenu> menus = new ArrayList<>();
        for (int i = 0; i < MENU_NAMES.length; i++) {
            menus.add(new PopMenu(i, MENU_ICONS[i], MENU_NAMES[i], false));
        }
        menuWindow.setData(menus);
        if (type == Media.DOCUMENT) {
            popMenu = menus.get(0);
        }
        if (type == Media.IMAGE) {
            popMenu = menus.get(1);
        }
        if (type == Media.VIDEO) {
            popMenu = menus.get(2);
        }
        if (type == Media.AUDIO) {
            popMenu = menus.get(3);
        }
        group_down.setEnabled(toggle ? true : false);
        iv_down.setVisibility(toggle ? View.VISIBLE : View.GONE);
        tv_name.setText(popMenu.getName());
        showPopMenuDocument(popMenu);
        //=================[搜索]=================
        et_search.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_close) {
            finish();
        } else if (id == R.id.group_down) {
            if (menuWindow.isShowing()) {
                menuWindow.dismiss();
                return;
            }
            showPopMenuWindow(group_down);
            iv_down.setRotation(180);
        } else if (id == R.id.iv_search) {
            group_navigation.setVisibility(View.GONE);
            group_search.setVisibility(View.VISIBLE);
        } else if (id == R.id.iv_cancel) {
            if (et_search.getText().toString().length() > 0) {
                et_search.setText("");
            }
            group_navigation.setVisibility(View.VISIBLE);
            group_search.setVisibility(View.GONE);
        } else if (id == R.id.v_cover) {
            menuWindow.dismiss();
        } else if (id == R.id.tv_confirm) {
            setResult();
        }
    }

    /**
     * 设置返回结果
     */
    protected void setResult() {
        List<Media> list = getChecked();
        int count = list == null ? 0 : list.size();
        if (count == 0) {
            showToast("请选择文件");
            return;
        }
        Intent data = new Intent();
        data.putExtra("result", (ArrayList) list);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 获取适配器
     *
     * @return
     */
    protected MediaAdapter getAdapter() {
        return adapter;
    }

    /**
     * 获取当前菜单
     *
     * @return
     */
    protected PopMenu getPopMenu() {
        return popMenu;
    }

    /**
     * 设置当前弹出菜单item
     *
     * @param popMenu 弹出菜单item
     */
    protected void setPopMenu(PopMenu popMenu) {
        this.popMenu = popMenu;
    }

    /**
     * 显示弹出菜单
     *
     * @param v
     */
    protected void showPopMenuWindow(View v) {
        v_cover.setVisibility(View.VISIBLE);
        menuWindow.setOnMediaMenuItemClickListener(menu -> {
            setPopMenu(menu);
            tv_name.setText(menu.getName());
            menuWindow.dismiss();
            showPopMenuDocument(menu);
        });
        menuWindow.setOnDismissListener(() -> {
            v_cover.setVisibility(View.GONE);
            iv_down.setRotation(0);
        });
        menuWindow.showAsDropDown(v);
    }

    /**
     * 显示菜单
     *
     * @param menu 菜单
     */
    protected void showPopMenuDocument(PopMenu menu) {
        int id = menu.getId();
        if (id == 0) {
            showFileDocument();
        }
        if (id == 1) {
            showImageDocument();
        }
        if (id == 2) {
            showVideoDocument();
        }
        if (id == 3) {
            showAudioDocument();
        }
    }

    /**
     * 显示手机存储文件
     */
    protected void showFileDocument() {
        stack = new Stack<>();
        adapter.setViewType(MediaAdapter.ITEM_FILE);
        rv_document.setLayoutManager(new LinearLayoutManager(this));
        rv_document.setAdapter(adapter);
        adapter.setData(files);
    }

    /**
     * 显示图片文件
     */
    protected void showImageDocument() {
        stack = new Stack<>();
        adapter.setViewType(MediaAdapter.ITEM_IMAGE);
        rv_document.setLayoutManager(new GridLayoutManager(this, 3));
        rv_document.setAdapter(adapter);
        adapter.setData(images);
    }

    /**
     * 显示视频文件
     */
    protected void showVideoDocument() {
        stack = new Stack<>();
        adapter.setViewType(MediaAdapter.ITEM_VIDEO);
        rv_document.setLayoutManager(new GridLayoutManager(this, 3));
        rv_document.setAdapter(adapter);
        adapter.setData(videos);
    }


    /**
     * 显示视频文件
     */
    protected void showAudioDocument() {
        stack = new Stack<>();
        adapter.setViewType(MediaAdapter.ITEM_AUDIO);
        rv_document.setLayoutManager(new LinearLayoutManager(this));
        rv_document.setAdapter(adapter);
        adapter.setData(audios);
    }

    @Override
    public void onBackPressed() {
        if (stack.size() > 0) {
            adapter.setData(stack.pop());
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaItemClick(MediaAdapter adapter, View v, int position) {
        int id = v.getId();//选中
        if (id == R.id.check_group || id == R.id.iv_check) {
            Media document = adapter.getItem(position);
            if (isMultiple() && max > -1 && getCheckedCount() >= max) {
                if (document.isCheck()) {
                    adapter.check(position);
                    setCheckedItem(adapter.getItem(position));
                } else {
                    showToast("最多选择" + max + "个文件");
                }
                return;
            }
            adapter.check(position);
            setCheckedItem(adapter.getItem(position));
        } else if (id == R.id.item_view) {//文件点击
            Media item = adapter.getItem(position);
            TBSActivity.start(this, new File(item.getData()));
        }
    }

    /**
     * 是否多选
     *
     * @return
     */
    protected boolean isMultiple() {
        return multiple;
    }

    /**
     * 获取选中个数
     *
     * @return
     */
    protected int getCheckedCount() {
        List<Media> list = getChecked();
        return list == null ? 0 : list.size();
    }

    /**
     * 获取选中的文档
     *
     * @return
     */
    protected List<Media> getChecked() {
        return checked;
    }

    /**
     * 设置选中文档item
     *
     * @param document 文档
     */
    protected void setCheckedItem(Media document) {
        boolean isChecked = document.isCheck();
        if (isChecked) {
            if (!isMultiple()) {
                checked = new ArrayList<>();
            }
            checked.add(document);
        } else {
            for (int i = 0; i < checked.size(); i++) {
                if (checked.get(i).getId() == document.getId()) {
                    checked.remove(i);
                }
            }
        }
    }

    /**
     * 显示提示
     *
     * @param msg
     */
    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
            showPopMenuDocument(getPopMenu());
        } else {
            adapter.setData(MediaProvider.search(adapter.getData(), s.toString()));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
