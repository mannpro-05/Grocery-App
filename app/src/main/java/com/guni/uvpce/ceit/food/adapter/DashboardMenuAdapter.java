package com.guni.uvpce.ceit.food.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.guni.uvpce.ceit.food.R;
import com.guni.uvpce.ceit.food.model.DashboardMenu;

import java.util.ArrayList;

public class DashboardMenuAdapter extends RecyclerView.Adapter<DashboardMenuAdapter.ViewHolder> {
    private ArrayList<DashboardMenu> dataSet;
    private Context mContext;
    public DashboardMenuAdapter(@NonNull Context context,
                              @NonNull ArrayList<DashboardMenu> objects) {
        mContext = context; dataSet = objects;
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtName;
        ViewHolder(View view)
        {
            super(view);
            txtName = view.findViewById(R.id.dashboard_menu_txt);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dashboard_single_item,null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtName.setText(dataSet.get(position).sTextName);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
