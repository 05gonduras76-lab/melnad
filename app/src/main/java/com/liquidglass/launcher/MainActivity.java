package com.liquidglass.launcher;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        AppGridAdapter.AppInteractionListener,
        DesktopPagerAdapter.DesktopGestureListener,
        PackageChangeReceiver.OnPackagesChangedListener {

    private static final int APPS_PER_PAGE = 24;
    private static final int MAX_DOCK_APPS = 4;

    private StorageHelper storageHelper;
    private PackageChangeReceiver packageReceiver;

    private ViewPager2 viewPager;
    private LinearLayout pageIndicator;
    private RecyclerView rvDock;
    private View dockGlassBackground;

    private FrameLayout spotlightOverlay;
    private LinearLayout spotlightPanel;
    private View searchGlassBackground;
    private View resultsGlassBackground;
    private EditText etSearch;
    private RecyclerView rvSearchResults;

    private DesktopPagerAdapter pagerAdapter;
    private AppGridAdapter dockAdapter;
    private AppGridAdapter searchAdapter;

    private List<AppInfo> allApps = new ArrayList<>();
    private final List<List<AppInfo>> desktopPages = new ArrayList<>();
    private final List<AppInfo> dockApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        setContentView(R.layout.activity_main);

        storageHelper = new StorageHelper(this);
        initViews();
        setupInsets();
        setupAdapters();
        applyGlassTheme();
        reloadAppsAndLayout();

        packageReceiver = new PackageChangeReceiver(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, PackageChangeReceiver.createIntentFilter(), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(packageReceiver, PackageChangeReceiver.createIntentFilter());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (spotlightOverlay.getVisibility() == View.VISIBLE) {
                    closeSpotlight();
                } else if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0, true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (packageReceiver != null) {
            unregisterReceiver(packageReceiver);
        }
    }

    private void setupEdgeToEdge() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        pageIndicator = findViewById(R.id.pageIndicator);
        rvDock = findViewById(R.id.rvDock);
        dockGlassBackground = findViewById(R.id.dockGlassBackground);

        spotlightOverlay = findViewById(R.id.spotlightOverlay);
        spotlightPanel = findViewById(R.id.spotlightPanel);
        searchGlassBackground = findViewById(R.id.searchGlassBackground);
        resultsGlassBackground = findViewById(R.id.resultsGlassBackground);
        etSearch = findViewById(R.id.etSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            float scale = 1f - (absPos * 0.06f);
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(1f - (absPos * 0.25f));
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePageDots(position);
            }
        });

        spotlightOverlay.setOnClickListener(v -> closeSpotlight());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearchApps(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupInsets() {
        View root = findViewById(R.id.rootContainer);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            View desktopContent = findViewById(R.id.desktopContent);
            desktopContent.setPadding(0, topInset, 0, bottomInset);
            spotlightPanel.setPadding(
                    spotlightPanel.getPaddingLeft(),
                    topInset,
                    spotlightPanel.getPaddingRight(),
                    bottomInset
            );
            return windowInsets;
        });
    }

    private void setupAdapters() {
        boolean hideLabels = storageHelper.isHideLabels();

        pagerAdapter = new DesktopPagerAdapter(desktopPages, hideLabels, this, this);
        viewPager.setAdapter(pagerAdapter);

        rvDock.setLayoutManager(new GridLayoutManager(this, MAX_DOCK_APPS));
        dockAdapter = new AppGridAdapter(dockApps, hideLabels, true, this);
        rvDock.setAdapter(dockAdapter);

        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 4));
        searchAdapter = new AppGridAdapter(allApps, false, false, this);
        rvSearchResults.setAdapter(searchAdapter);
    }

    private void applyGlassTheme() {
        boolean darkGlass = storageHelper.isDarkGlass();
        boolean blurEnabled = storageHelper.isBlurEnabled();

        LiquidGlassHelper.applyGlassSurface(
                dockGlassBackground, LiquidGlassHelper.SurfaceType.DOCK, darkGlass, blurEnabled
        );
        LiquidGlassHelper.applyGlassSurface(
                searchGlassBackground, LiquidGlassHelper.SurfaceType.SEARCH, darkGlass, blurEnabled
        );
        LiquidGlassHelper.applyGlassSurface(
                resultsGlassBackground, LiquidGlassHelper.SurfaceType.DOCK, darkGlass, blurEnabled
        );
    }

    private void reloadAppsAndLayout() {
        allApps = AppLoader.loadInstalledApps(this);
        Map<String, AppInfo> appMap = AppLoader.toComponentMap(allApps);

        List<String> savedDockKeys = storageHelper.loadDock();
        List<List<String>> savedPageKeys = storageHelper.loadPages();

        dockApps.clear();
        desktopPages.clear();
        Set<String> assignedKeys = new HashSet<>();

        for (String key : savedDockKeys) {
            AppInfo info = appMap.get(key);
            if (info != null && dockApps.size() < MAX_DOCK_APPS && !assignedKeys.contains(key)) {
                dockApps.add(info);
                assignedKeys.add(key);
            }
        }

        if (savedDockKeys.isEmpty() && savedPageKeys.isEmpty()) {
            for (int i = 0; i < Math.min(MAX_DOCK_APPS, allApps.size()); i++) {
                AppInfo info = allApps.get(i);
                dockApps.add(info);
                assignedKeys.add(info.getComponentKey());
            }
        }

        for (List<String> pageKeys : savedPageKeys) {
            List<AppInfo> pageApps = new ArrayList<>();
            for (String key : pageKeys) {
                AppInfo info = appMap.get(key);
                if (info != null && !assignedKeys.contains(key)) {
                    pageApps.add(info);
                    assignedKeys.add(key);
                }
            }
            if (!pageApps.isEmpty()) {
                desktopPages.add(pageApps);
            }
        }

        for (AppInfo app : allApps) {
            if (!assignedKeys.contains(app.getComponentKey())) {
                addAppToFirstAvailablePage(app);
                assignedKeys.add(app.getComponentKey());
            }
        }

        if (desktopPages.isEmpty()) {
            desktopPages.add(new ArrayList<>());
        }

        persistCurrentLayout();
        refreshUI();
    }

    private void addAppToFirstAvailablePage(AppInfo app) {
        for (List<AppInfo> page : desktopPages) {
            if (page.size() < APPS_PER_PAGE) {
                page.add(app);
                return;
            }
        }
        List<AppInfo> newPage = new ArrayList<>();
        newPage.add(app);
        desktopPages.add(newPage);
    }

    private void persistCurrentLayout() {
        List<List<String>> pageKeys = new ArrayList<>();
        for (List<AppInfo> page : desktopPages) {
            List<String> keys = new ArrayList<>();
            for (AppInfo app : page) {
                keys.add(app.getComponentKey());
            }
            pageKeys.add(keys);
        }

        List<String> dockKeys = new ArrayList<>();
        for (AppInfo app : dockApps) {
            dockKeys.add(app.getComponentKey());
        }

        storageHelper.saveLayout(pageKeys, dockKeys);
    }

    private void refreshUI() {
        boolean hideLabels = storageHelper.isHideLabels();
        pagerAdapter.updatePages(desktopPages, hideLabels);
        dockAdapter.updateData(dockApps, hideLabels);
        buildPageDots(desktopPages.size(), viewPager.getCurrentItem());
    }

    private void buildPageDots(int count, int activeIndex) {
        pageIndicator.removeAllViews();
        if (count <= 1) return;

        int marginPx = (int) (4 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
            pageIndicator.addView(dot);
        }
    }

    private void updatePageDots(int activeIndex) {
        int count = pageIndicator.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = pageIndicator.getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setImageResource(
                        i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive
                );
            }
        }
    }

    @Override
    public void onAppClick(AppInfo app, View itemView) {
        if (spotlightOverlay.getVisibility() == View.VISIBLE) {
            closeSpotlight();
        }
        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchIntent.setComponent(new ComponentName(app.packageName, app.activityName));
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            startActivity(launchIntent);
        } catch (Exception e) {
            Toast.makeText(this, "РќРµ СѓРґР°Р»РѕСЃСЊ Р·Р°РїСѓСЃС‚РёС‚СЊ " + app.label, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAppLongClick(AppInfo app, View itemView) {
        showAppContextMenu(app);
    }

    @SuppressLint("InflateParams")
    private void showAppContextMenu(AppInfo app) {
        Dialog dialog = new Dialog(this, R.style.GlassDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_context_menu, null);
        dialog.setContentView(view);

        View menuBg = view.findViewById(R.id.menuGlassBackground);
        LiquidGlassHelper.applyGlassSurface(
                menuBg,
                LiquidGlassHelper.SurfaceType.MENU,
                storageHelper.isDarkGlass(),
                storageHelper.isBlurEnabled()
        );

        TextView tvTitle = view.findViewById(R.id.tvMenuTitle);
        TextView btnToggleDock = view.findViewById(R.id.btnToggleDock);
        TextView btnAppInfo = view.findViewById(R.id.btnAppInfo);
        TextView btnUninstall = view.findViewById(R.id.btnUninstall);

        tvTitle.setText(app.label);
        boolean inDock = dockApps.contains(app);
        btnToggleDock.setText(inDock ? R.string.remove_from_dock : R.string.add_to_dock);

        btnToggleDock.setOnClickListener(v -> {
            dialog.dismiss();
            toggleAppInDock(app);
        });

        btnAppInfo.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + app.packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        btnUninstall.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + app.packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        dialog.show();
        LiquidGlassHelper.animateSpringScale(view, 1.0f);
    }

    private void toggleAppInDock(AppInfo app) {
        if (dockApps.contains(app)) {
            dockApps.remove(app);
            addAppToFirstAvailablePage(app);
        } else {
            if (dockApps.size() >= MAX_DOCK_APPS) {
                Toast.makeText(this, R.string.dock_full, Toast.LENGTH_SHORT).show();
                return;
            }
            for (List<AppInfo> page : desktopPages) {
                page.remove(app);
            }
            for (int i = desktopPages.size() - 1; i > 0; i--) {
                if (desktopPages.get(i).isEmpty()) {
                    desktopPages.remove(i);
                }
            }
            dockApps.add(app);
        }
        persistCurrentLayout();
        refreshUI();
    }

    @Override
    public void onEmptySpaceLongPress() {
        showLauncherSettingsDialog();
    }

    @SuppressLint("InflateParams")
    private void showLauncherSettingsDialog() {
        Dialog dialog = new Dialog(this, R.style.GlassDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialog.setContentView(view);

        View settingsBg = view.findViewById(R.id.settingsGlassBackground);
        LiquidGlassHelper.applyGlassSurface(
                settingsBg,
                LiquidGlassHelper.SurfaceType.MENU,
                true,
                storageHelper.isBlurEnabled()
        );

        SwitchCompat switchBlur = view.findViewById(R.id.switchRealtimeBlur);
        SwitchCompat switchDark = view.findViewById(R.id.switchDarkGlass);
        SwitchCompat switchLabels = view.findViewById(R.id.switchHideLabels);
        TextView btnClose = view.findViewById(R.id.btnCloseSettings);

        switchBlur.setChecked(storageHelper.isBlurEnabled());
        switchDark.setChecked(storageHelper.isDarkGlass());
        switchLabels.setChecked(storageHelper.isHideLabels());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            switchBlur.setEnabled(false);
        }

        switchBlur.setOnCheckedChangeListener((buttonView, isChecked) -> {
            storageHelper.setBlurEnabled(isChecked);
            applyGlassTheme();
        });

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            storageHelper.setDarkGlass(isChecked);
            applyGlassTheme();
        });

        switchLabels.setOnCheckedChangeListener((buttonView, isChecked) -> {
            storageHelper.setHideLabels(isChecked);
            refreshUI();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        dialog.show();
        LiquidGlassHelper.animateSpringScale(view, 1.0f);
    }

    @Override
    public void onSwipeDown() {
        openSpotlight();
    }

    private void openSpotlight() {
        if (spotlightOverlay.getVisibility() == View.VISIBLE) return;

        etSearch.setText("");
        searchAdapter.updateData(allApps, false);

        spotlightOverlay.setAlpha(0f);
        spotlightOverlay.setVisibility(View.VISIBLE);
        spotlightOverlay.animate().alpha(1f).setDuration(200).start();

        spotlightPanel.setTranslationY(-80f);
        spotlightPanel.animate().translationY(0f).setDuration(240).start();

        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void closeSpotlight() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
        spotlightOverlay.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction(() -> spotlightOverlay.setVisibility(View.GONE))
                .start();
    }

    private void filterSearchApps(@NonNull String query) {
        String trimmed = query.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            searchAdapter.updateData(allApps, false);
            return;
        }
        List<AppInfo> filtered = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (app.label.toLowerCase().contains(trimmed)
                    || app.packageName.toLowerCase().contains(trimmed)) {
                filtered.add(app);
            }
        }
        searchAdapter.updateData(filtered, false);
    }

    @Override
    public void onPackagesChanged() {
        reloadAppsAndLayout();
    }
}