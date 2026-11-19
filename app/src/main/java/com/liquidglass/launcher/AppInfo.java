package com.liquidglass.launcher;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppInfo {
    public final String label;
    public final String packageName;
    public final String activityName;
    public final Drawable icon;
    public final boolean isFolder;
    public final String folderId;
    public final List<AppInfo> folderApps;

    public AppInfo(String label, String packageName, String activityName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.activityName = activityName;
        this.icon = icon;
        this.isFolder = false;
        this.folderId = null;
        this.folderApps = new ArrayList<>();
    }

    public AppInfo(String folderId, String folderName, Drawable coverIcon, List<AppInfo> folderApps) {
        this.label = folderName;
        this.packageName = "folder:" + folderId;
        this.activityName = "FolderActivity";
        this.icon = coverIcon;
        this.isFolder = true;
        this.folderId = folderId;
        this.folderApps = new ArrayList<>(folderApps);
    }

    public String getComponentKey() {
        if (isFolder) {
            return "folder:" + folderId;
        }
        return packageName + "/" + activityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppInfo appInfo = (AppInfo) o;
        return isFolder == appInfo.isFolder &&
                Objects.equals(getComponentKey(), appInfo.getComponentKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFolder, getComponentKey());
    }
}