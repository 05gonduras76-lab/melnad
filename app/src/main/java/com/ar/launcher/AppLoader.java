package com.ar.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppLoader {

    public static List<AppInfo> loadInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        List<AppInfo> apps = new ArrayList<>();
        String selfPackage = context.getPackageName();

        for (ResolveInfo info : resolveInfos) {
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (selfPackage.equals(pkg)) continue;

            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null ? labelSeq.toString() : pkg;
            String activityName = info.activityInfo.name;
            Drawable icon = info.loadIcon(pm);

            apps.add(new AppInfo(label, pkg, activityName, icon));
        }

        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a, AppInfo b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });

        return apps;
    }

    public static Map<String, AppInfo> toComponentMap(List<AppInfo> apps) {
        Map<String, AppInfo> map = new HashMap<>();
        for (AppInfo app : apps) {
            map.put(app.getComponentKey(), app);
        }
        return map;
    }
}