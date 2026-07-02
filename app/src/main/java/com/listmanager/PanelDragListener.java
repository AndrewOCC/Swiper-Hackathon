package com.listmanager;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Drives ViewPager2 panel scrolling from touch drags so content follows the finger,
 * with velocity-based fling on release. Finger/drag left reveals the column on the right.
 */
public class PanelDragListener {

    public interface Host {
        @NonNull
        ViewPager2 getViewPager();
    }

    private final Host host;
    private final int touchSlop;
    private final int minFlingVelocity;

    private float lastX;
    private float lastY;
    private float downX;
    private float downY;
    private boolean dragging;
    private boolean tracking;
    private VelocityTracker velocityTracker;

    public PanelDragListener(@NonNull Host host) {
        this.host = host;
        ViewConfiguration configuration = ViewConfiguration.get(host.getViewPager().getContext());
        touchSlop = configuration.getScaledTouchSlop();
        minFlingVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    public View.OnTouchListener asTouchListener() {
        return this::onTouch;
    }

    public RecyclerView.OnItemTouchListener asRecyclerBlankAreaListener() {
        return new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView,
                                                 @NonNull MotionEvent event) {
                if (recyclerView.findChildViewUnder(event.getX(), event.getY()) != null) {
                    return false;
                }
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    onTouch(recyclerView, event);
                    return false;
                }
                return onTouch(recyclerView, event);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView,
                                     @NonNull MotionEvent event) {
                onTouch(recyclerView, event);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };
    }

    private boolean onTouch(View view, MotionEvent event) {
        ViewPager2 viewPager = host.getViewPager();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                tracking = true;
                dragging = false;
                downX = event.getX();
                downY = event.getY();
                lastX = downX;
                lastY = downY;
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                return false;

            case MotionEvent.ACTION_MOVE:
                if (!tracking || velocityTracker == null) {
                    return false;
                }
                velocityTracker.addMovement(event);

                float deltaX = event.getX() - lastX;
                float totalDeltaX = event.getX() - downX;
                float totalDeltaY = event.getY() - downY;

                if (!dragging) {
                    if (Math.abs(totalDeltaX) <= touchSlop
                            || Math.abs(totalDeltaX) <= Math.abs(totalDeltaY)) {
                        lastX = event.getX();
                        lastY = event.getY();
                        return false;
                    }
                    dragging = viewPager.beginFakeDrag();
                }

                if (dragging) {
                    viewPager.fakeDragBy(deltaX);
                }
                lastX = event.getX();
                lastY = event.getY();
                return dragging;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean wasDragging = dragging;
                if (velocityTracker != null) {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);
                }
                if (dragging) {
                    viewPager.endFakeDrag();
                    applyFling(viewPager);
                }
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                tracking = false;
                dragging = false;
                return wasDragging;

            default:
                return false;
        }
    }

    private void applyFling(@NonNull ViewPager2 viewPager) {
        if (velocityTracker == null) {
            return;
        }
        float velocityX = velocityTracker.getXVelocity();
        if (Math.abs(velocityX) < minFlingVelocity) {
            return;
        }

        int current = viewPager.getCurrentItem();
        if (velocityX < 0) {
            scrollToPanel(viewPager, current + 1);
        } else {
            scrollToPanel(viewPager, current - 1);
        }
    }

    private void scrollToPanel(@NonNull ViewPager2 viewPager, int index) {
        if (index < 0 || index > 2) {
            return;
        }
        viewPager.setCurrentItem(index, true);
    }
}
