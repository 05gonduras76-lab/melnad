package com.liquidglass.launcher;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.AppViewHolder> {

    public interface AppInteractionListener {
        void onAppClick(AppInfo app, View itemView);
        void onAppLongClick(AppInfo app, View itemView);
    }

    public static class StyleConfig {
        public final boolean hideLabels;
        public final int iconSizeDp;
        public final int iconShape;
        public final int iconDesign;
        public final boolean monochrome;
        public final int iconTint;

        public StyleConfig(boolean hideLabels,
                           int iconSizeDp,
                           int iconShape,
                           int iconDesign,
                           boolean monochrome,
                           int iconTint) {
            this.hideLabels = hideLabels;
            this.iconSizeDp = iconSizeDp;
            this.iconShape = iconShape;
            this.iconDesign = iconDesign;
            this.monochrome = monochrome;
            this.iconTint = iconTint;
        }
    }

    private List<AppInfo> apps = new ArrayList<>();
    private final AppInteractionListener listener;
    private StyleConfig styleConfig;
    private final boolean isDock;

    public AppGridAdapter(List<AppInfo> apps,
                          StyleConfig styleConfig,
                          boolean isDock,
                          AppInteractionListener listener) {
        if (apps != null) {
            this.apps = new ArrayList<>(apps);
        }
        this.styleConfig = styleConfig;
        this.isDock = isDock;
        this.listener = listener;
    }

    public void updateData(List<AppInfo> newApps, StyleConfig styleConfig) {
        this.apps = new ArrayList<>(newApps);
        this.styleConfig = styleConfig;
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
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;
        int sizePx = (int) (styleConfig.iconSizeDp * density);

        ViewGroup.LayoutParams frameParams = holder.iconFrame.getLayoutParams();
        frameParams.width = sizePx;
        frameParams.height = sizePx;
        holder.iconFrame.setLayoutParams(frameParams);

        holder.ivIcon.setImageDrawable(app.icon);
        LiquidGlassHelper.applyIconAppearance(
                holder.ivIcon,
                holder.glassRimOverlay,
                styleConfig.iconShape,
                styleConfig.iconDesign,
                app.isFolder ? false : styleConfig.monochrome,
                app.isFolder ? 0 : styleConfig.iconTint
        );

        if (app.isFolder) {
            holder.folderBadgeOverlay.setVisibility(View.VISIBLE);
            holder.tvFolderCount.setVisibility(View.VISIBLE);
            holder.tvFolderCount.setText(String.valueOf(app.folderApps.size()));
        } else {
            holder.folderBadgeOverlay.setVisibility(View.GONE);
            holder.tvFolderCount.setVisibility(View.GONE);
        }

        if (isDock || styleConfig.hideLabels) {
            holder.tvLabel.setVisibility(View.GONE);
        } else {
            holder.tvLabel.setVisibility(View.VISIBLE);
            holder.tvLabel.setText(app.label);
        }

        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LiquidGlassHelper.animateSpringScale(v, 0.86f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    LiquidGlassHelper.animateSpringScale(v, 1.0f);
                    break;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            LiquidGlassHelper.animateSpringScale(v, 1.12f);
            v.postDelayed(() -> LiquidGlassHelper.animateSpringScale(v, 1.0f), 140);
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
        final FrameLayout iconFrame;
        final ShapeableImageView ivIcon;
        final View glassRimOverlay;
        final View folderBadgeOverlay;
        final TextView tvFolderCount;
        final TextView tvLabel;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            iconFrame = itemView.findViewById(R.id.iconFrame);
            ivIcon = itemView.findViewById(R.id.ivAppIcon);
            glassRimOverlay = itemView.findViewById(R.id.glassRimOverlay);
            folderBadgeOverlay = itemView.findViewById(R.id.folderBadgeOverlay);
            tvFolderCount = itemView.findViewById(R.id.tvFolderCount);
            tvLabel = itemView.findViewById(R.id.tvAppLabel);
        }
    }
}