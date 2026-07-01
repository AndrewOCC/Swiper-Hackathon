package com.listmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.listmanager.model.ItemSwipeAction;
import com.listmanager.model.ListCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ListItemSwipeTouchListener implements RecyclerView.OnItemTouchListener {

    public interface Callback {
        void onSwipeLeft(int position);

        void onSwipeRight(int position);
    }

    private final RecyclerView recyclerView;
    private final Callback callback;
    private final Supplier<ListCategory> categorySupplier;
    private final int moveBackgroundColor;
    private final int deleteBackgroundColor;
    private final int deleteTextColor;

    private final int slop;
    private final int minFlingVelocity;
    private final int maxFlingVelocity;
    private final long animationTime;

    private int viewWidth = 1;
    private float downX;
    private float downY;
    private float finalDelta;
    private float alpha;
    private boolean swiping;
    private int swipingSlop;
    private int downPosition = ListView.INVALID_POSITION;
    private int animatingPosition = ListView.INVALID_POSITION;
    private View downView;
    private View foregroundView;
    private LinearLayout backgroundLeft;
    private LinearLayout backgroundRight;
    private TextView leftLabel;
    private TextView rightLabel;
    private ImageView rightIcon;
    private ItemSwipeAction activeAction;
    private boolean paused;
    private VelocityTracker velocityTracker;

    private final List<PendingDismissData> pendingDismisses = new ArrayList<>();
    private int dismissAnimationRefCount = 0;

    public ListItemSwipeTouchListener(@NonNull RecyclerView recyclerView,
                                      @NonNull Supplier<ListCategory> categorySupplier,
                                      @NonNull Callback callback) {
        this.recyclerView = recyclerView;
        this.categorySupplier = categorySupplier;
        this.callback = callback;

        Context context = recyclerView.getContext();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        slop = configuration.getScaledTouchSlop();
        minFlingVelocity = configuration.getScaledMinimumFlingVelocity() * 16;
        maxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        animationTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        moveBackgroundColor = ContextCompat.getColor(context, R.color.swipe_move_background);
        deleteBackgroundColor = ContextCompat.getColor(context, R.color.swipe_delete_background);
        deleteTextColor = ContextCompat.getColor(context, android.R.color.white);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                paused = newState == RecyclerView.SCROLL_STATE_DRAGGING;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
        return handleTouchEvent(event);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
        handleTouchEvent(event);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private boolean handleTouchEvent(MotionEvent event) {
        if (viewWidth < 2) {
            viewWidth = recyclerView.getWidth();
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (paused) {
                    break;
                }
                downView = findTouchedChild(event);
                if (downView != null
                        && animatingPosition != recyclerView.getChildAdapterPosition(downView)) {
                    bindSwipeViews(downView);
                    downPosition = recyclerView.getChildAdapterPosition(downView);
                    if (downPosition != RecyclerView.NO_POSITION) {
                        alpha = foregroundView.getAlpha();
                        downX = event.getRawX();
                        downY = event.getRawY();
                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(event);
                    } else {
                        resetGestureState(false);
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                cancelSwipeAnimation();
                resetGestureState(true);
                break;

            case MotionEvent.ACTION_UP:
                if (velocityTracker == null) {
                    break;
                }

                finalDelta = event.getRawX() - downX;
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = velocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(velocityTracker.getYVelocity());

                boolean dismiss = false;
                boolean dismissToRight = false;
                if (Math.abs(finalDelta) > viewWidth / 3f && swiping && activeAction != null
                        && activeAction.type != ItemSwipeAction.Type.NONE) {
                    dismiss = true;
                    dismissToRight = finalDelta > 0;
                } else if (minFlingVelocity <= absVelocityX && absVelocityX <= maxFlingVelocity
                        && absVelocityY < absVelocityX && swiping && activeAction != null
                        && activeAction.type != ItemSwipeAction.Type.NONE) {
                    dismiss = (velocityX < 0) == (finalDelta < 0);
                    dismissToRight = velocityX > 0;
                }

                if (dismiss && downPosition != animatingPosition && downPosition != ListView.INVALID_POSITION) {
                    animateCompletion(dismissToRight);
                } else {
                    cancelSwipeAnimation();
                }
                resetGestureState(true);
                break;

            case MotionEvent.ACTION_MOVE:
                if (velocityTracker == null || paused || foregroundView == null) {
                    break;
                }

                velocityTracker.addMovement(event);
                float deltaX = event.getRawX() - downX;
                float deltaY = event.getRawY() - downY;

                if (!swiping && Math.abs(deltaX) > slop && Math.abs(deltaY) < Math.abs(deltaX) / 2f) {
                    activeAction = resolveAction(deltaX);
                    if (activeAction.type == ItemSwipeAction.Type.NONE) {
                        resetGestureState(false);
                        break;
                    }
                    applyBackground(activeAction, deltaX < 0);
                    swiping = true;
                    swipingSlop = deltaX > 0 ? slop : -slop;
                }

                if (swiping && activeAction != null) {
                    float translation = deltaX - swipingSlop;
                    foregroundView.setTranslationX(translation);
                    if (activeAction.type == ItemSwipeAction.Type.DELETE) {
                        float progress = Math.min(1f, Math.abs(translation) / (viewWidth * 0.75f));
                        foregroundView.setAlpha(Math.max(0.2f, alpha * (1f - progress * 0.8f)));
                    } else {
                        foregroundView.setAlpha(alpha);
                    }
                    return true;
                }
                break;

            default:
                break;
        }

        return swiping;
    }

    private View findTouchedChild(MotionEvent event) {
        Rect rect = new Rect();
        int[] coords = new int[2];
        recyclerView.getLocationOnScreen(coords);
        int x = (int) event.getRawX() - coords[0];
        int y = (int) event.getRawY() - coords[1];

        for (int i = recyclerView.getChildCount() - 1; i >= 0; i--) {
            View child = recyclerView.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                return child;
            }
        }
        return null;
    }

    private void bindSwipeViews(View itemView) {
        foregroundView = itemView.findViewById(R.id.swipe_foreground);
        backgroundLeft = itemView.findViewById(R.id.swipe_background_left);
        backgroundRight = itemView.findViewById(R.id.swipe_background_right);
        leftLabel = itemView.findViewById(R.id.swipe_left_label);
        rightLabel = itemView.findViewById(R.id.swipe_right_label);
        rightIcon = itemView.findViewById(R.id.swipe_right_icon);
    }

    private ItemSwipeAction resolveAction(float deltaX) {
        ListCategory category = categorySupplier.get();
        if (category == null) {
            category = ListCategory.INBOX;
        }
        if (deltaX < 0) {
            return ItemSwipeAction.forLeftSwipe(category, moveBackgroundColor);
        }
        return ItemSwipeAction.forRightSwipe(category, moveBackgroundColor);
    }

    private void applyBackground(ItemSwipeAction action, boolean swipingLeft) {
        backgroundLeft.setVisibility(View.GONE);
        backgroundRight.setVisibility(View.GONE);
        leftLabel.setVisibility(View.GONE);
        rightLabel.setVisibility(View.GONE);
        rightIcon.setVisibility(View.GONE);

        LinearLayout container = swipingLeft ? backgroundRight : backgroundLeft;
        TextView label = swipingLeft ? rightLabel : leftLabel;
        ImageView icon = swipingLeft ? rightIcon : null;

        container.setVisibility(View.VISIBLE);
        container.setBackgroundColor(action.type == ItemSwipeAction.Type.DELETE
                ? deleteBackgroundColor
                : action.backgroundColor);

        if (action.type == ItemSwipeAction.Type.DELETE) {
            if (icon != null) {
                icon.setVisibility(View.VISIBLE);
            }
            label.setText(action.labelRes);
            label.setTextColor(deleteTextColor);
            label.setVisibility(View.VISIBLE);
        } else if (action.type == ItemSwipeAction.Type.MOVE) {
            label.setText(action.labelRes);
            label.setTextColor(ContextCompat.getColor(recyclerView.getContext(), R.color.swipe_move_text));
            label.setVisibility(View.VISIBLE);
        }
    }

    private void animateCompletion(boolean dismissToRight) {
        final View itemView = downView;
        final View foreground = foregroundView;
        final int position = downPosition;
        final ItemSwipeAction action = activeAction;
        final boolean swipeRight = dismissToRight;

        animatingPosition = position;
        dismissAnimationRefCount++;

        float targetTranslation = dismissToRight ? viewWidth : -viewWidth;
        foreground.animate().cancel();
        foreground.animate()
                .translationX(targetTranslation)
                .alpha(action.type == ItemSwipeAction.Type.DELETE ? 0f : alpha)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        performDismiss(itemView, position, swipeRight);
                    }
                })
                .start();
    }

    private void cancelSwipeAnimation() {
        if (foregroundView != null) {
            foregroundView.animate().cancel();
            foregroundView.animate()
                    .translationX(0)
                    .alpha(alpha)
                    .setDuration(animationTime)
                    .setListener(null)
                    .start();
        }
        hideBackgrounds();
    }

    private void hideBackgrounds() {
        if (backgroundLeft != null) {
            backgroundLeft.setVisibility(View.GONE);
        }
        if (backgroundRight != null) {
            backgroundRight.setVisibility(View.GONE);
        }
    }

    private void performDismiss(final View dismissView, final int dismissPosition, final boolean swipeRight) {
        final ViewGroup.LayoutParams layoutParams = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();
        final int originalLayoutHeight = layoutParams.height;

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dismissAnimationRefCount--;
                if (dismissAnimationRefCount == 0) {
                    Collections.sort(pendingDismisses);
                    for (PendingDismissData pendingDismiss : pendingDismisses) {
                        if (pendingDismiss.swipeRight) {
                            callback.onSwipeRight(pendingDismiss.position);
                        } else {
                            callback.onSwipeLeft(pendingDismiss.position);
                        }

                        pendingDismiss.view.setAlpha(alpha);
                        pendingDismiss.view.setTranslationX(0);
                        ViewGroup.LayoutParams params = pendingDismiss.view.getLayoutParams();
                        params.height = originalLayoutHeight;
                        pendingDismiss.view.setLayoutParams(params);
                        hideBackgroundsOnView(pendingDismiss.view);
                    }

                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(
                            time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    recyclerView.dispatchTouchEvent(cancelEvent);

                    pendingDismisses.clear();
                    animatingPosition = ListView.INVALID_POSITION;
                }
            }
        });
        animator.addUpdateListener(valueAnimator -> {
            layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
            dismissView.setLayoutParams(layoutParams);
        });

        pendingDismisses.add(new PendingDismissData(dismissPosition, dismissView, swipeRight));
        animator.start();
    }

    private void hideBackgroundsOnView(View itemView) {
        itemView.findViewById(R.id.swipe_background_left).setVisibility(View.GONE);
        itemView.findViewById(R.id.swipe_background_right).setVisibility(View.GONE);
    }

    private void resetGestureState(boolean recycleTracker) {
        if (recycleTracker && velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
        downX = 0;
        downY = 0;
        downView = null;
        foregroundView = null;
        backgroundLeft = null;
        backgroundRight = null;
        leftLabel = null;
        rightLabel = null;
        rightIcon = null;
        activeAction = null;
        downPosition = ListView.INVALID_POSITION;
        swiping = false;
    }

    private static class PendingDismissData implements Comparable<PendingDismissData> {
        final int position;
        final View view;
        final boolean swipeRight;

        PendingDismissData(int position, View view, boolean swipeRight) {
            this.position = position;
            this.view = view;
            this.swipeRight = swipeRight;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            return other.position - position;
        }
    }
}
