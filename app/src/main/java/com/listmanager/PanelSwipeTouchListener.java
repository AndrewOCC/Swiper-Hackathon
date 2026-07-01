package com.listmanager;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;

public class PanelSwipeTouchListener implements View.OnTouchListener {

    public interface Callback {
        void onSwipeLeft();

        void onSwipeRight();
    }

    private final GestureDetectorCompat gestureDetector;

    public PanelSwipeTouchListener(@NonNull Context context, @NonNull Callback callback) {
        gestureDetector = new GestureDetectorCompat(
                context,
                new GestureDetector.SimpleOnGestureListener() {
                    private static final int SWIPE_THRESHOLD = 80;
                    private static final int SWIPE_VELOCITY_THRESHOLD = 80;

                    @Override
                    public boolean onDown(MotionEvent event) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent first, MotionEvent second, float velocityX, float velocityY) {
                        if (first == null || second == null) {
                            return false;
                        }

                        float deltaX = second.getX() - first.getX();
                        float deltaY = second.getY() - first.getY();

                        if (Math.abs(deltaX) < Math.abs(deltaY)) {
                            return false;
                        }

                        if (Math.abs(deltaX) < SWIPE_THRESHOLD
                                && Math.abs(velocityX) < SWIPE_VELOCITY_THRESHOLD) {
                            return false;
                        }

                        if (deltaX > 0) {
                            callback.onSwipeRight();
                        } else {
                            callback.onSwipeLeft();
                        }
                        return true;
                    }
                }
        );
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }
}
