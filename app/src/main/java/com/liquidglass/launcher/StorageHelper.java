package com.liquidglass.launcher;

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

    private static final String PREFS_NAME = "liquid_glass_prefs_v2";
    private static final String KEY_PAGES = "desktop_pages_json";
    private static final String KEY_DOCK = "dock_apps_json";
    private static final String KEY_FOLDERS = "folders_json";
    private static final String KEY_BLUR_ENABLED = "pref_blur_enabled";
    private static final String KEY_DARK_GLASS = "pref_dark_glass";
    private static final String KEY_HIDE_LABELS = "pref_hide_labels";
    private static final String KEY_ICON_SIZE_DP = "pref_icon_size_dp";
    private static final String KEY_ICON_TINT = "pref_icon_tint";

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

    public int getIconSizeDp() {
        return prefs.getInt(KEY_ICON_SIZE_DP, 60);
    }

    public void setIconSizeDp(int sizeDp) {
        prefs.edit().putInt(KEY_ICON_SIZE_DP, sizeDp).apply();
    }

    public int getIconTintColor() {
        return prefs.getInt(KEY_ICON_TINT, 0);
    }

    public void setIconTintColor(int color) {
        prefs.edit().putInt(KEY_ICON_TINT, color).apply();
    }
}