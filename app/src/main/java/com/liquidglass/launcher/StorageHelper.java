package com.liquidglass.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class StorageHelper {

    private static final String PREFS_NAME = "liquid_glass_prefs";
    private static final String KEY_PAGES = "desktop_pages_json";
    private static final String KEY_DOCK = "dock_apps_json";
    private static final String KEY_BLUR_ENABLED = "pref_blur_enabled";
    private static final String KEY_DARK_GLASS = "pref_dark_glass";
    private static final String KEY_HIDE_LABELS = "pref_hide_labels";

    private final SharedPreferences prefs;

    public StorageHelper(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLayout(List<List<String>> pages, List<String> dockKeys) {
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

        prefs.edit()
                .putString(KEY_PAGES, pagesArray.toString())
                .putString(KEY_DOCK, dockArray.toString())
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
}