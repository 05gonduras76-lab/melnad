package com.liquidglass.launcher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class LiquidGlassHelper {

    public enum SurfaceType {
        DOCK,
        SEARCH,
        MENU
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

    public static void applyIconTint(ImageView imageView, int tintColor) {
        if (tintColor == 0) {
            imageView.clearColorFilter();
        } else {
            imageView.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
        }
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

    public static void startActivityWithLaunchAnimation(Activity activity, Intent intent, View sourceView) {
        if (sourceView != null) {
            int width = Math.max(1, sourceView.getWidth());
            int height = Math.max(1, sourceView.getHeight());
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
                    sourceView, 0, 0, width, height
            );
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }
}