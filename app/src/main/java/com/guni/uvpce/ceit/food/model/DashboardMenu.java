package com.guni.uvpce.ceit.food.model;

public class DashboardMenu {
    public String sTextName;
    public int group_pos;
    public int child_pos;
    public DashboardMenu(String sName,int g_p,int c_p)
    {
        sTextName = sName;
        group_pos = g_p;
        child_pos = c_p;
    }
}
