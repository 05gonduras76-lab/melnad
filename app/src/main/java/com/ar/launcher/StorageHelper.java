package com.ar.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StorageHelper {

    private static final String PREFS_NAME = "ar_launcher_prefs_v4";
    private static final String KEY_PAGES = "desktop_pages_json";
    private static final String KEY_DOCK = "dock_apps_json";
    private static final String KEY_FOLDERS = "folders_json";
    private static final String KEY_BLUR_ENABLED = "pref_blur_enabled";
    private static final String KEY_DARK_GLASS = "pref_dark_glass";
    private static final String KEY_HIDE_LABELS = "pref_hide_labels";
    private static final String KEY_MONOCHROME = "pref_monochrome";
    private static final String KEY_CLOCK_WIDGET = "pref_clock_widget";
    private static final String KEY_APPWIDGET_ID = "pref_appwidget_id";
    private static final String KEY_ICON_SIZE_DP = "pref_icon_size_dp";
    private static final String KEY_ICON_SHAPE = "pref_icon_shape";
    private static final String KEY_ICON_DESIGN = "pref_icon_design";
    private static final String KEY_DOCK_STYLE = "pref_dock_style";
    private static final String KEY_ICON_TINT = "pref_icon_tint";
    private static final String KEY_ANIM_STYLE = "pref_anim_style";
    private static final String KEY_ANIM_DURATION = "pref_anim_duration";
    private static final String KEY_ANIM_BLUR = "pref_anim_blur";

    private final SharedPreferences prefs;

    public StorageHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLayout(List<List<String>> pages, List<String> dockKeys, Map<String, List<String>> folders) {
        JSONArray pagesArray = new JSONArray();
        for (List<String> page : pages) {
            JSONArray pageArr = new JSONArray();
            for (String key : page) {
                pageArr.put(key);
            }
            pagesArray.put(pageArr);
        }

        JSONArray dockArray = new JSONArray();
        for (String key : dockKeys) {
            dockArray.put(key);
        }

        JSONObject foldersObj = new JSONObject();
        for (Map.Entry<String, List<String>> entry : folders.entrySet()) {
            JSONArray arr = new JSONArray();
            for (String appKey : entry.getValue()) {
                arr.put(appKey);
            }
            try {
                foldersObj.put(entry.getKey(), arr);
            } catch (JSONException ignored) {
            }
        }

        prefs.edit()
                .putString(KEY_PAGES, pagesArray.toString())
                .putString(KEY_DOCK, dockArray.toString())
                .putString(KEY_FOLDERS, foldersObj.toString())
                .apply();
    }

    public List<List<String>> loadPages() {
        List<List<String>> result = new ArrayList<>();
        String json = prefs.getString(KEY_PAGES, null);
        if (json == null) return result;

        try {
            JSONArray pagesArray = new JSONArray(json);
            for (int i = 0; i < pagesArray.length(); i++) {
                JSONArray pageArr = pagesArray.getJSONArray(i);
                List<String> page = new ArrayList<>();
                for (int j = 0; j < pageArr.length(); j++) {
                    page.add(pageArr.getString(j));
                }
                result.add(page);
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public List<String> loadDock() {
        List<String> result = new ArrayList<>();
        String json = prefs.getString(KEY_DOCK, null);
        if (json == null) return result;

        try {
            JSONArray dockArray = new JSONArray(json);
            for (int i = 0; i < dockArray.length(); i++) {
                result.add(dockArray.getString(i));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public Map<String, List<String>> loadFolders() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        String json = prefs.getString(KEY_FOLDERS, null);
        if (json == null) return result;

        try {
            JSONObject obj = new JSONObject(json);
            JSONArray names = obj.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    String folderId = names.getString(i);
                    JSONArray arr = obj.getJSONArray(folderId);
                    List<String> keys = new ArrayList<>();
                    for (int j = 0; j < arr.length(); j++) {
                        keys.add(arr.getString(j));
                    }
                    result.put(folderId, keys);
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    public boolean isBlurEnabled() {
        return prefs.getBoolean(KEY_BLUR_ENABLED, true);
    }

    public void setBlurEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BLUR_ENABLED, enabled).apply();
    }

    public boolean isDarkGlass() {
        return prefs.getBoolean(KEY_DARK_GLASS, false);
    }

    public void setDarkGlass(boolean dark) {
        prefs.edit().putBoolean(KEY_DARK_GLASS, dark).apply();
    }

    public boolean isHideLabels() {
        return prefs.getBoolean(KEY_HIDE_LABELS, false);
    }

    public void setHideLabels(boolean hide) {
        prefs.edit().putBoolean(KEY_HIDE_LABELS, hide).apply();
    }

    public boolean isMonochrome() {
        return prefs.getBoolean(KEY_MONOCHROME, false);
    }

    public void setMonochrome(boolean mono) {
        prefs.edit().putBoolean(KEY_MONOCHROME, mono).apply();
    }

    public boolean isClockWidgetEnabled() {
        return prefs.getBoolean(KEY_CLOCK_WIDGET, true);
    }

    public void setClockWidgetEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_CLOCK_WIDGET, enabled).apply();
    }

    public int getAppWidgetId() {
        return prefs.getInt(KEY_APPWIDGET_ID, -1);
    }

    public void setAppWidgetId(int id) {
        prefs.edit().putInt(KEY_APPWIDGET_ID, id).apply();
    }

    public int getIconSizeDp() {
        return prefs.getInt(KEY_ICON_SIZE_DP, 60);
    }

    public void setIconSizeDp(int sizeDp) {
        prefs.edit().putInt(KEY_ICON_SIZE_DP, sizeDp).apply();
    }

    public int getIconShape() {
        return prefs.getInt(KEY_ICON_SHAPE, 0);
    }

    public void setIconShape(int shapeIndex) {
        prefs.edit().putInt(KEY_ICON_SHAPE, shapeIndex).apply();
    }

    public int getIconDesign() {
        return prefs.getInt(KEY_ICON_DESIGN, 0);
    }

    public void setIconDesign(int designIndex) {
        prefs.edit().putInt(KEY_ICON_DESIGN, designIndex).apply();
    }

    public int getDockStyle() {
        return prefs.getInt(KEY_DOCK_STYLE, 0);
    }

    public void setDockStyle(int dockStyleIndex) {
        prefs.edit().putInt(KEY_DOCK_STYLE, dockStyleIndex).apply();
    }

    public int getIconTintColor() {
        return prefs.getInt(KEY_ICON_TINT, 0);
    }

    public void setIconTintColor(int color) {
        prefs.edit().putInt(KEY_ICON_TINT, color).apply();
    }

    public int getAnimStyle() {
        return prefs.getInt(KEY_ANIM_STYLE, 0);
    }

    public void setAnimStyle(int styleIndex) {
        prefs.edit().putInt(KEY_ANIM_STYLE, styleIndex).apply();
    }

    public int getAnimDurationMs() {
        return prefs.getInt(KEY_ANIM_DURATION, 330);
    }

    public void setAnimDurationMs(int durationMs) {
        prefs.edit().putInt(KEY_ANIM_DURATION, durationMs).apply();
    }

    public int getAnimBlurRadius() {
        return prefs.getInt(KEY_ANIM_BLUR, 28);
    }

    public void setAnimBlurRadius(int blurRadius) {
        prefs.edit().putInt(KEY_ANIM_BLUR, blurRadius).apply();
    }
}