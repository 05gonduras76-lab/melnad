package com.liquidglass.launcher;

import android.graphics.drawable.Drawable;

import java.util.Objects;

public class AppInfo {
    public final String label;
    public final String packageName;
    public final String activityName;
    public final Drawable icon;

    public AppInfo(String label, String packageName, String activityName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.activityName = activityName;
        this.icon = icon;
    }

    public String getComponentKey() {
        return packageName + "/" + activityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppInfo appInfo = (AppInfo) o;
        return Objects.equals(packageName, appInfo.packageName) &&
                Objects.equals(activityName, appInfo.activityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, activityName);
    }
}