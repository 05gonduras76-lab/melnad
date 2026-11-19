package com.ar.launcher;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;

public class LiquidGlassHelper {

    public enum SurfaceType {
        DOCK,
        SEARCH,
        MENU
    }

    private static ValueAnimator blurAnimator;

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
                float radius = (type == SurfaceType.MENU) ? 32f : 22f;
                targetView.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP));
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
                dockBgView.setRenderEffect(RenderEffect.createBlurEffect(24f, 24f, Shader.TileMode.CLAMP));
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
                ShapeAppearanceModel.builder(
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

    public static int extractEffectiveIconColor(Drawable drawable, boolean monochrome, int tintColor) {
        int baseColor = Color.parseColor("#383840");
        if (drawable != null) {
            try {
                Bitmap bmp;
                if (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap() != null) {
                    bmp = Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), 24, 24, true);
                } else {
                    bmp = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmp);
                    drawable.setBounds(0, 0, 24, 24);
                    drawable.draw(canvas);
                }

                long rSum = 0;
                long gSum = 0;
                long bSum = 0;
                int count = 0;
                for (int x = 0; x < bmp.getWidth(); x += 2) {
                    for (int y = 0; y < bmp.getHeight(); y += 2) {
                        int px = bmp.getPixel(x, y);
                        int alpha = Color.alpha(px);
                        if (alpha > 120) {
                            rSum += Color.red(px);
                            gSum += Color.green(px);
                            bSum += Color.blue(px);
                            count++;
                        }
                    }
                }
                if (count > 0) {
                    baseColor = Color.rgb(
                            (int) (rSum / count),
                            (int) (gSum / count),
                            (int) (bSum / count)
                    );
                }
            } catch (Exception ignored) {
            }
        }

        if (monochrome) {
            int lum = (int) (0.299f * Color.red(baseColor)
                    + 0.587f * Color.green(baseColor)
                    + 0.114f * Color.blue(baseColor));
            baseColor = Color.rgb(lum, lum, lum);
        }

        if (tintColor != 0) {
            int tr = Color.red(tintColor);
            int tg = Color.green(tintColor);
            int tb = Color.blue(tintColor);
            baseColor = Color.rgb(
                    (Color.red(baseColor) + tr) / 2,
                    (Color.green(baseColor) + tg) / 2,
                    (Color.blue(baseColor) + tb) / 2
            );
        }

        return baseColor;
    }

    public static void animateFullScreenBlur(View desktopView,
                                              View scrimView,
                                              boolean enableBlur,
                                              boolean wallpaperZoom,
                                              int maxBlurRadius,
                                              int durationMs) {
        if (blurAnimator != null && blurAnimator.isRunning()) {
            blurAnimator.cancel();
        }

        float targetRadius = enableBlur ? Math.max(4f, maxBlurRadius) : 0f;
        float startRadius = 0f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startRadius = enableBlur ? 0.5f : targetRadius;
        }

        PathInterpolator smoothCurve = new PathInterpolator(0.22f, 1.0f, 0.36f, 1.0f);

        blurAnimator = ValueAnimator.ofFloat(startRadius, enableBlur ? targetRadius : 0.5f);
        blurAnimator.setDuration(durationMs);
        blurAnimator.setInterpolator(smoothCurve);
        blurAnimator.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (enableBlur || val > 1.0f) {
                    desktopView.setRenderEffect(RenderEffect.createBlurEffect(val, val, Shader.TileMode.CLAMP));
                } else {
                    desktopView.setRenderEffect(null);
                }
            }
        });
        blurAnimator.start();

        float targetScale = 1.0f;
        if (enableBlur && wallpaperZoom) {
            targetScale = 1.06f;
        }

        desktopView.animate()
                .alpha(enableBlur ? 0.72f : 1.0f)
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(durationMs)
                .setInterpolator(smoothCurve)
                .withEndAction(() -> {
                    if (!enableBlur) {
                        desktopView.setScaleX(1.0f);
                        desktopView.setScaleY(1.0f);
                    }
                })
                .start();

        if (scrimView != null) {
            scrimView.animate()
                    .alpha(enableBlur ? 0.36f : 0f)
                    .setDuration(durationMs)
                    .setInterpolator(smoothCurve)
                    .start();
        }
    }

    public static void animateSpringScale(View view, float targetScale) {
        SpringAnimation animX = new SpringAnimation(view, DynamicAnimation.SCALE_X, targetScale);
        SpringAnimation animY = new SpringAnimation(view, DynamicAnimation.SCALE_Y, targetScale);

        SpringForce forceX = new SpringForce(targetScale)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
        SpringForce forceY = new SpringForce(targetScale)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);

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
