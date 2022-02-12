package androidx.io.core.media;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    private View itemView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public View itemView(){
        return itemView;
    }

    public <T extends View> T findView(@IdRes int id) {
        return itemView.findViewById(id);
    }


}
