package com.liquidglass.launcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.material.imageview.ShapeableImageView;

public class LiquidGlassHelper {

    public enum SurfaceType {
        DOCK,
        SEARCH,
        MENU
    }

    public interface TransitionCallbacks {
        void onPrepareLaunch(AppInfo app, View sourceView, Intent launchIntent);
        void onResumeReturn();
    }

    public static void applyGlassSurface(View targetView, SurfaceType type, boolean darkGlass, boolean blurEnabled) {
        @DrawableRes int bgRes;
        switch (type) {
            case SEARCH:
                bgRes = darkGlass ? R.drawable.bg_search_dark : R.drawable.bg_search_light;
                break;
            case MENU:
                bgRes = darkGlass ? R.drawable.bg_context_menu_dark : R.drawable.bg_context_menu_light;
                break;
            case DOCK:
            default:
                bgRes = darkGlass ? R.drawable.bg_dock_dark : R.drawable.bg_dock_light;
                break;
        }
        targetView.setBackgroundResource(bgRes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (blurEnabled) {
                float radius = (type == SurfaceType.MENU) ? 38f : 26f;
                RenderEffect blurEffect = RenderEffect.createBlurEffect(
                        radius, radius, Shader.TileMode.CLAMP
                );
                targetView.setRenderEffect(blurEffect);
            } else {
                targetView.setRenderEffect(null);
            }
        }
    }

    public static void applyDockStyle(View dockBgView, int dockStyleIndex, boolean darkGlass, boolean blurEnabled) {
        if (dockStyleIndex == 3) {
            dockBgView.setBackgroundColor(0x00000000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dockBgView.setRenderEffect(null);
            }
            return;
        }
        if (dockStyleIndex == 1) {
            dockBgView.setBackgroundResource(R.drawable.bg_dock_translucent);
        } else if (dockStyleIndex == 2) {
            dockBgView.setBackgroundResource(R.drawable.bg_dock_dark);
        } else {
            dockBgView.setBackgroundResource(darkGlass ? R.drawable.bg_dock_dark : R.drawable.bg_dock_light);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (blurEnabled) {
                dockBgView.setRenderEffect(RenderEffect.createBlurEffect(28f, 28f, Shader.TileMode.CLAMP));
            } else {
                dockBgView.setRenderEffect(null);
            }
        }
    }

    @StyleRes
    public static int resolveShapeOverlay(int shapeIndex) {
        switch (shapeIndex) {
            case 1:
                return R.style.ShapeCircle;
            case 2:
                return R.style.ShapeSquare;
            case 3:
                return R.style.ShapeOneUi;
            case 4:
                return R.style.ShapeOriginOs;
            case 5:
                return R.style.ShapeColorOs;
            case 0:
            default:
                return R.style.ShapeSquircle;
        }
    }

    public static void applyIconAppearance(ShapeableImageView iconView,
                                           View rimView,
                                           int shapeIndex,
                                           int designIndex,
                                           boolean monochrome,
                                           int tintColor) {
        iconView.setShapeAppearanceModel(
                com.google.android.material.shape.ShapeAppearanceModel.builder(
                        iconView.getContext(), resolveShapeOverlay(shapeIndex), 0
                ).build()
        );

        if (designIndex == 2) {
            int pad = (int) (6 * iconView.getContext().getResources().getDisplayMetrics().density);
            iconView.setPadding(pad, pad, pad, pad);
        } else {
            iconView.setPadding(0, 0, 0, 0);
        }

        if (rimView != null) {
            rimView.setVisibility(designIndex == 1 ? View.VISIBLE : View.GONE);
        }

        if (monochrome) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            iconView.setColorFilter(new ColorMatrixColorFilter(cm));
        } else if (tintColor != 0) {
            iconView.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            iconView.clearColorFilter();
        }
    }

    public static void setBackgroundBlur(View targetView, boolean enableBlur) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (enableBlur) {
                targetView.setRenderEffect(RenderEffect.createBlurEffect(26f, 26f, Shader.TileMode.CLAMP));
            } else {
                targetView.setRenderEffect(null);
            }
        }
        targetView.animate()
                .alpha(enableBlur ? 0.55f : 1.0f)
                .scaleX(enableBlur ? 0.96f : 1.0f)
                .scaleY(enableBlur ? 0.96f : 1.0f)
                .setDuration(240)
                .start();
    }

    public static void animateSpringScale(View view, float targetScale) {
        SpringAnimation animX = new SpringAnimation(view, DynamicAnimation.SCALE_X, targetScale);
        SpringAnimation animY = new SpringAnimation(view, DynamicAnimation.SCALE_Y, targetScale);

        SpringForce forceX = new SpringForce(targetScale)
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        SpringForce forceY = new SpringForce(targetScale)
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

        animX.setSpring(forceX);
        animY.setSpring(forceY);
        animX.start();
        animY.start();
    }

    public static void startActivitySmooth(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
