package com.liquidglass.launcher;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.AppViewHolder> {

    public interface AppInteractionListener {
        void onAppClick(AppInfo app, View itemView);
        void onAppLongClick(AppInfo app, View itemView);
    }

    private List<AppInfo> apps = new ArrayList<>();
    private final AppInteractionListener listener;
    private boolean hideLabels;
    private final boolean isDock;

    public AppGridAdapter(List<AppInfo> apps, boolean hideLabels, boolean isDock, AppInteractionListener listener) {
        if (apps != null) {
            this.apps = new ArrayList<>(apps);
        }
        this.hideLabels = hideLabels;
        this.isDock = isDock;
        this.listener = listener;
    }

    public void updateData(List<AppInfo> newApps, boolean hideLabels) {
        this.apps = new ArrayList<>(newApps);
        this.hideLabels = hideLabels;
        notifyDataSetChanged();
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels = hideLabels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = apps.get(position);
        holder.ivIcon.setImageDrawable(app.icon);

        if (isDock || hideLabels) {
            holder.tvLabel.setVisibility(View.GONE);
        } else {
            holder.tvLabel.setVisibility(View.VISIBLE);
            holder.tvLabel.setText(app.label);
        }

        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LiquidGlassHelper.animateSpringScale(v, 0.88f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    LiquidGlassHelper.animateSpringScale(v, 1.0f);
                    break;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(app, v);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            LiquidGlassHelper.animateSpringScale(v, 1.08f);
            v.postDelayed(() -> LiquidGlassHelper.animateSpringScale(v, 1.0f), 160);
            if (listener != null) {
                listener.onAppLongClick(app, v);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvLabel;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivAppIcon);
            tvLabel = itemView.findViewById(R.id.tvAppLabel);
        }
    }
}