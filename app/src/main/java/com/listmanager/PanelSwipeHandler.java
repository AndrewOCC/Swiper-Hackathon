package com.listmanager;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public class PanelSwipeHandler {

    public interface Callback {
        void onSwipeLeft();

        void onSwipeRight();
    }

    private final Callback callback;
    private final int swipeThresholdPx;
    private final float directionRatio;
    private final GestureDetectorCompat gestureDetector;

    private float downX;
    private float downY;
    private boolean swipeTriggered;

    public PanelSwipeHandler(@NonNull Context context,
                               @NonNull Callback callback,
                               int swipeThresholdPx) {
        this.callback = callback;
        this.swipeThresholdPx = swipeThresholdPx;
        this.directionRatio = 1.2f;
        this.gestureDetector = new GestureDetectorCompat(
                context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent first,
                                           MotionEvent second,
                                           float velocityX,
                                           float velocityY) {
                        if (first == null || second == null || swipeTriggered) {
                            return false;
                        }

                        float deltaX = second.getX() - first.getX();
                        float deltaY = second.getY() - first.getY();

                        if (Math.abs(deltaX) < Math.abs(deltaY) * directionRatio) {
                            return false;
                        }

                        if (Math.abs(deltaX) < swipeThresholdPx
                                && Math.abs(velocityX) < swipeThresholdPx) {
                            return false;
                        }

                        triggerSwipe(deltaX);
                        return true;
                    }
                }
        );
    }

    public View.OnTouchListener asTouchListener() {
        return this::handleTouch;
    }

    public RecyclerView.OnItemTouchListener asRecyclerBlankAreaListener() {
        return new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView,
                                                 @NonNull MotionEvent event) {
                View child = recyclerView.findChildViewUnder(event.getX(), event.getY());
                if (child != null) {
                    resetIfNeeded(event);
                    return false;
                }
                return handleTouch(recyclerView, event);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView,
                                     @NonNull MotionEvent event) {
                handleTouch(recyclerView, event);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
    }

    private boolean handleTouch(View view, MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                swipeTriggered = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (!swipeTriggered) {
                    float deltaX = event.getX() - downX;
                    float deltaY = event.getY() - downY;
                    if (Math.abs(deltaX) > swipeThresholdPx
                            && Math.abs(deltaX) > Math.abs(deltaY) * directionRatio) {
                        triggerSwipe(deltaX);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetIfNeeded(event);
                break;

            default:
                break;
        }

        return swipeTriggered;
    }

    private void resetIfNeeded(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            swipeTriggered = false;
        }
    }

    private void triggerSwipe(float deltaX) {
        if (swipeTriggered) {
            return;
        }
        swipeTriggered = true;
        if (deltaX > 0) {
            callback.onSwipeRight();
        } else {
            callback.onSwipeLeft();
        }
    }
}
