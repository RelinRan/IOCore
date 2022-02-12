package androidx.io.core.media;

public class PopMenu {

    /**
     * 菜单ID
     */
    private int id;
    /**
     * 菜单图标
     */
    private int icon;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 是否选中
     */
    private boolean check;

    public PopMenu(int id,int icon, String name, boolean check) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.check = check;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
