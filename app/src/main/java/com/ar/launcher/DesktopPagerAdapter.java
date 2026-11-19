package com.ar.launcher;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetHostView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DesktopPagerAdapter extends RecyclerView.Adapter<DesktopPagerAdapter.PageViewHolder> {

    public interface DesktopGestureListener {
        void onEmptySpaceLongPress();
        void onSwipeDown();
    }

    private List<List<AppInfo>> pages = new ArrayList<>();
    private AppGridAdapter.StyleConfig styleConfig;
    private boolean showClockWidget;
    private AppWidgetHostView externalWidgetView;
    private final AppGridAdapter.AppInteractionListener appListener;
    private final DesktopGestureListener gestureListener;

    public DesktopPagerAdapter(List<List<AppInfo>> pages,
                               AppGridAdapter.StyleConfig styleConfig,
                               boolean showClockWidget,
                               AppWidgetHostView externalWidgetView,
                               AppGridAdapter.AppInteractionListener appListener,
                               DesktopGestureListener gestureListener) {
        if (pages != null) {
            this.pages = pages;
        }
        this.styleConfig = styleConfig;
        this.showClockWidget = showClockWidget;
        this.externalWidgetView = externalWidgetView;
        this.appListener = appListener;
        this.gestureListener = gestureListener;
    }

    public void updatePages(List<List<AppInfo>> newPages,
                            AppGridAdapter.StyleConfig styleConfig,
                            boolean showClockWidget,
                            AppWidgetHostView externalWidgetView) {
        this.pages = newPages;
        this.styleConfig = styleConfig;
        this.showClockWidget = showClockWidget;
        this.externalWidgetView = externalWidgetView;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_desktop, parent, false);
        return new PageViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        boolean isFirstPage = (position == 0);
        holder.clockWidgetContainer.setVisibility(isFirstPage && showClockWidget ? View.VISIBLE : View.GONE);

        holder.customWidgetSlot.removeAllViews();
        if (isFirstPage && externalWidgetView != null) {
            if (externalWidgetView.getParent() != null) {
                ((ViewGroup) externalWidgetView.getParent()).removeView(externalWidgetView);
            }
            holder.customWidgetSlot.addView(externalWidgetView);
            holder.customWidgetSlot.setVisibility(View.VISIBLE);
        } else {
            holder.customWidgetSlot.setVisibility(View.GONE);
        }

        List<AppInfo> pageApps = pages.get(position);
        holder.rvGrid.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 4));
        AppGridAdapter adapter = new AppGridAdapter(pageApps, styleConfig, false, appListener);
        holder.rvGrid.setAdapter(adapter);

        GestureDetector detector = new GestureDetector(holder.itemView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(@NonNull MotionEvent e) {
                        View child = holder.rvGrid.findChildViewUnder(e.getX(), e.getY());
                        if (child == null && gestureListener != null) {
                            gestureListener.onEmptySpaceLongPress();
                        }
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                        if (e1 != null) {
                            float diffY = e2.getY() - e1.getY();
                            float diffX = Math.abs(e2.getX() - e1.getX());
                            if (diffY > 120 && diffY > diffX && Math.abs(velocityY) > 250) {
                                if (gestureListener != null) {
                                    gestureListener.onSwipeDown();
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });

        holder.rvGrid.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout clockWidgetContainer;
        final FrameLayout customWidgetSlot;
        final RecyclerView rvGrid;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            clockWidgetContainer = itemView.findViewById(R.id.clockWidgetContainer);
            customWidgetSlot = itemView.findViewById(R.id.customWidgetSlot);
            rvGrid = itemView.findViewById(R.id.rvPageGrid);
        }
    }
}