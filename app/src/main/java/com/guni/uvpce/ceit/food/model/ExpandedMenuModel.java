package com.guni.uvpce.ceit.food.model;

import java.util.ArrayList;
import java.util.List;

public class ExpandedMenuModel {
    String iconName = "";
    int iconImg = -1; // menu icon resource id
    List<String> listDataChild;
    public ExpandedMenuModel()
    {
        listDataChild = new ArrayList<>();
    }
    public List<String> getChildList(){return listDataChild;}
    public String getIconName() {
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    public int getIconImg() {
        return iconImg;
    }
    public void setIconImg(int iconImg) {
        this.iconImg = iconImg;
    }
}
