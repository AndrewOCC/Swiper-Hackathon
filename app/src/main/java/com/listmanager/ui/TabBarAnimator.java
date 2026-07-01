package com.listmanager.ui;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class TabBarAnimator {

    private static final float SELECTED_ALPHA = 1f;
    private static final float UNSELECTED_ALPHA = 0.4f;

    private final TextView[] tabs;
    private final View indicator;
    @ColorInt
    private final int selectedColor;
    @ColorInt
    private final int unselectedColor;

    private int tabWidth;
    private int indicatorWidth;

    public TabBarAnimator(@NonNull TextView tabLowPriority,
                          @NonNull TextView tabInbox,
                          @NonNull TextView tabHighPriority,
                          @NonNull View indicator,
                          @ColorInt int selectedColor,
                          @ColorInt int unselectedColor) {
        this.tabs = new TextView[]{tabLowPriority, tabInbox, tabHighPriority};
        this.indicator = indicator;
        this.selectedColor = selectedColor;
        this.unselectedColor = unselectedColor;

        indicator.post(this::measureAndApplyInitialState);
        tabs[0].addOnLayoutChangeListener((view, left, top, right, bottom,
                                           oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left != oldRight - oldLeft) {
                measureAndApplyInitialState();
            }
        });
    }

    public void remeasure() {
        measureAndApplyInitialState();
    }

    public void onPageScrolled(int position, float offset) {
        if (tabWidth == 0) {
            measureIndicator();
        }
        float scrollPosition = position + offset;
        updateIndicator(scrollPosition);
        updateTabStyles(scrollPosition);
    }

    private void measureAndApplyInitialState() {
        measureIndicator();
        onPageScrolled(1, 0f);
    }

    private void measureIndicator() {
        if (tabs[0].getWidth() == 0) {
            return;
        }
        tabWidth = tabs[0].getWidth();
        indicatorWidth = tabWidth - tabs[0].getPaddingLeft() - tabs[0].getPaddingRight();
        if (indicatorWidth <= 0) {
            indicatorWidth = tabWidth;
        }

        indicator.setVisibility(View.VISIBLE);
        android.view.ViewGroup.MarginLayoutParams params =
                (android.view.ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
        params.width = indicatorWidth;
        params.leftMargin = tabs[0].getLeft() + tabs[0].getPaddingLeft();
        indicator.setLayoutParams(params);
    }

    private void updateIndicator(float scrollPosition) {
        if (tabWidth == 0) {
            return;
        }

        android.view.ViewGroup.MarginLayoutParams params =
                (android.view.ViewGroup.MarginLayoutParams) indicator.getLayoutParams();
        int baseLeft = tabs[0].getLeft() + tabs[0].getPaddingLeft();
        params.leftMargin = baseLeft + Math.round(scrollPosition * tabWidth);
        indicator.setLayoutParams(params);
    }

    private void updateTabStyles(float scrollPosition) {
        for (int i = 0; i < tabs.length; i++) {
            float distance = Math.abs(scrollPosition - i);
            float selection = Math.max(0f, 1f - distance);
            float alpha = UNSELECTED_ALPHA + (SELECTED_ALPHA - UNSELECTED_ALPHA) * selection;
            tabs[i].setAlpha(alpha);
            tabs[i].setTypeface(null, selection > 0.5f ? Typeface.BOLD : Typeface.NORMAL);
            tabs[i].setTextColor(blendColors(unselectedColor, selectedColor, selection));
        }
    }

    @ColorInt
    private int blendColors(@ColorInt int from, @ColorInt int to, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int alpha = (int) (((from >> 24) & 0xFF) + (((to >> 24) & 0xFF) - ((from >> 24) & 0xFF)) * ratio);
        int red = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * ratio);
        int green = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * ratio);
        int blue = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * ratio);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
