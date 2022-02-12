package androidx.io.core.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.io.core.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class PopMenuWindow extends PopupWindow {

    private RecyclerView rv_menu;
    private PopMenuAdapter adapter;

    public PopMenuWindow(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.io_core_media_menu, null);
        rv_menu = contentView.findViewById(R.id.rv_menu);
        rv_menu.setLayoutManager(new LinearLayoutManager(context));
        adapter = new PopMenuAdapter(context);
        rv_menu.setAdapter(adapter);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        setOutsideTouchable(true);
        setContentView(contentView);
        setWidth(width);
        setHeight(height);
    }

    /**
     * 设置菜单文档Item点击监听
     *
     * @param onMediaMenuItemClickListener
     */
    public void setOnMediaMenuItemClickListener(OnMediaMenuItemClickListener onMediaMenuItemClickListener) {
        adapter.setOnMediaMenuItemClickListener(onMediaMenuItemClickListener);
    }

    /**
     * 设置数据源
     *
     * @param data
     */
    public void setData(List<PopMenu> data) {
        adapter.setData(data);
    }

}
