package com.listmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.listmanager.model.ItemSwipeAction;
import com.listmanager.model.ListCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Handles horizontal swipe gestures on individual list rows. Attached per item so
 * vertical scrolling and panel swipes are not blocked at the RecyclerView level.
 */
public class ListItemSwipeController {

    public interface Callback {
        void onSwipeLeft(int position);

        void onSwipeRight(int position);

        void onItemClick(int position);
    }

    private final RecyclerView recyclerView;
    private final Callback callback;
    private final Supplier<ListCategory> categorySupplier;
    private final Supplier<String> editingItemIdSupplier;
    private final IntFunction<String> itemIdAtPosition;
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
    private float swipeAnchorX;
    private float finalDelta;
    private float alpha;
    private boolean swiping;
    private int activePosition = RecyclerView.NO_POSITION;
    private int animatingPosition = RecyclerView.NO_POSITION;
    private View activeView;
    private View foregroundView;
    private LinearLayout backgroundLeft;
    private LinearLayout backgroundRight;
    private TextView leftLabel;
    private TextView rightLabel;
    private ImageView rightIcon;
    private ItemSwipeAction activeAction;
    private boolean panelNavigationBlocked;
    private VelocityTracker velocityTracker;

    private final List<PendingDismissData> pendingDismisses = new ArrayList<>();
    private int activeDismissAnimations = 0;

    public ListItemSwipeController(@NonNull RecyclerView recyclerView,
                                   @NonNull Supplier<ListCategory> categorySupplier,
                                   @NonNull Supplier<String> editingItemIdSupplier,
                                   @NonNull IntFunction<String> itemIdAtPosition,
                                   @NonNull Callback callback) {
        this.recyclerView = recyclerView;
        this.categorySupplier = categorySupplier;
        this.editingItemIdSupplier = editingItemIdSupplier;
        this.itemIdAtPosition = itemIdAtPosition;
        this.callback = callback;

        Context context = recyclerView.getContext();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        slop = configuration.getScaledTouchSlop();
        minFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        animationTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        moveBackgroundColor = ContextCompat.getColor(context, R.color.swipe_move_background);
        deleteBackgroundColor = ContextCompat.getColor(context, R.color.swipe_delete_background);
        deleteTextColor = ContextCompat.getColor(context, android.R.color.white);
    }

    public void attachToItem(@NonNull View itemView, int position) {
        itemView.setOnTouchListener((view, event) -> handleItemTouch(view, position, event));
    }

    private boolean handleItemTouch(@NonNull View itemView, int position, @NonNull MotionEvent event) {
        if (viewWidth < 2) {
            viewWidth = recyclerView.getWidth();
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (animatingPosition == position || isEditingPosition(position)) {
                    return false;
                }
                if (activePosition != RecyclerView.NO_POSITION
                        && activePosition != position
                        && !swiping) {
                    resetGestureState(true);
                }
                activePosition = position;
                activeView = itemView;
                bindSwipeViews(itemView);
                if (foregroundView == null) {
                    resetGestureState(false);
                    return false;
                }
                alpha = foregroundView.getAlpha();
                downX = event.getRawX();
                downY = event.getRawY();
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                }
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                return false;

            case MotionEvent.ACTION_CANCEL:
                if (activeView != itemView) {
                    return false;
                }
                cancelSwipeAnimation();
                resetGestureState(true);
                return swiping;

            case MotionEvent.ACTION_UP:
                if (activeView != itemView || velocityTracker == null) {
                    return false;
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

                boolean consumed = swiping;
                if (dismiss && position != animatingPosition) {
                    animateCompletion(dismissToRight);
                } else {
                    if (!swiping && !dismiss) {
                        float finalDeltaY = event.getRawY() - downY;
                        if (Math.abs(finalDelta) < slop && Math.abs(finalDeltaY) < slop) {
                            callback.onItemClick(position);
                            consumed = true;
                        }
                    }
                    cancelSwipeAnimation();
                }
                resetGestureState(true);
                return consumed;

            case MotionEvent.ACTION_MOVE:
                if (activeView != itemView || velocityTracker == null || !hasSwipeViews()) {
                    return false;
                }

                velocityTracker.addMovement(event);
                float deltaX = event.getRawX() - downX;
                float deltaY = event.getRawY() - downY;

                if (!swiping && Math.abs(deltaY) > slop && Math.abs(deltaY) > Math.abs(deltaX)) {
                    resetGestureState(true);
                    return false;
                }

                if (!swiping && Math.abs(deltaX) > slop && Math.abs(deltaY) < Math.abs(deltaX) / 2f) {
                    activeAction = resolveAction(deltaX);
                    if (activeAction.type == ItemSwipeAction.Type.NONE) {
                        resetGestureState(true);
                        return false;
                    }
                    applyBackground(activeAction, deltaX < 0);
                    swiping = true;
                    swipeAnchorX = event.getRawX();
                    blockPanelNavigation(true);
                    itemView.setTranslationZ(recyclerView.getResources().getDisplayMetrics().density * 8f);
                }

                if (swiping && activeAction != null) {
                    float translation = event.getRawX() - swipeAnchorX;
                    itemView.setTranslationX(translation);
                    if (activeAction.type == ItemSwipeAction.Type.DELETE) {
                        float progress = Math.min(1f, Math.abs(translation) / (viewWidth * 0.75f));
                        foregroundView.setAlpha(Math.max(0.2f, alpha * (1f - progress * 0.8f)));
                    } else {
                        foregroundView.setAlpha(alpha);
                    }
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    private boolean isEditingPosition(int position) {
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }
        String editingId = editingItemIdSupplier.get();
        if (editingId == null) {
            return false;
        }
        String itemId = itemIdAtPosition.apply(position);
        return editingId.equals(itemId);
    }

    private void bindSwipeViews(View itemView) {
        foregroundView = itemView.findViewById(R.id.swipe_foreground);
        backgroundLeft = itemView.findViewById(R.id.swipe_background_left);
        backgroundRight = itemView.findViewById(R.id.swipe_background_right);
        leftLabel = itemView.findViewById(R.id.swipe_left_label);
        rightLabel = itemView.findViewById(R.id.swipe_right_label);
        rightIcon = itemView.findViewById(R.id.swipe_right_icon);
    }

    private boolean hasSwipeViews() {
        return foregroundView != null
                && backgroundLeft != null
                && backgroundRight != null
                && leftLabel != null
                && rightLabel != null;
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
        final View itemView = activeView;
        final View foreground = foregroundView;
        final int position = activePosition;
        final ItemSwipeAction action = activeAction;
        final boolean swipeRight = dismissToRight;

        animatingPosition = position;
        activeDismissAnimations++;

        float targetTranslation = dismissToRight ? viewWidth : -viewWidth;
        float currentTranslation = itemView.getTranslationX();
        long remainingDuration = (long) (animationTime
                * (Math.abs(targetTranslation - currentTranslation) / viewWidth));
        if (remainingDuration < 50) {
            remainingDuration = animationTime;
        }

        foreground.animate().cancel();
        foreground.animate()
                .translationX(0f)
                .alpha(action.type == ItemSwipeAction.Type.DELETE ? 0f : alpha)
                .setDuration(remainingDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pendingDismisses.add(new PendingDismissData(position, itemView, swipeRight));
                        activeDismissAnimations--;
                        if (activeDismissAnimations == 0) {
                            finishPendingDismisses();
                        }
                    }
                })
                .start();
        itemView.animate().cancel();
        itemView.animate()
                .translationX(targetTranslation)
                .setDuration(remainingDuration)
                .start();
    }

    private void cancelSwipeAnimation() {
        if (activeView != null) {
            activeView.animate().cancel();
            activeView.animate()
                    .translationX(0)
                    .translationZ(0f)
                    .setDuration(animationTime)
                    .setListener(null)
                    .start();
        }
        if (foregroundView != null) {
            foregroundView.animate().cancel();
            foregroundView.animate()
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

    private void finishPendingDismisses() {
        Collections.sort(pendingDismisses);
        for (PendingDismissData pendingDismiss : pendingDismisses) {
            restoreDismissView(pendingDismiss.view);
            if (pendingDismiss.swipeRight) {
                callback.onSwipeRight(pendingDismiss.position);
            } else {
                callback.onSwipeLeft(pendingDismiss.position);
            }
        }
        pendingDismisses.clear();
        animatingPosition = RecyclerView.NO_POSITION;
    }

    private void restoreDismissView(View itemView) {
        View foreground = itemView.findViewById(R.id.swipe_foreground);
        if (foreground != null) {
            foreground.animate().cancel();
            foreground.setAlpha(1f);
        }

        itemView.animate().cancel();
        itemView.setTranslationX(0f);
        itemView.setTranslationZ(0f);

        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params != null) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            itemView.setLayoutParams(params);
        }
        hideBackgroundsOnView(itemView);
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
        if (swiping || panelNavigationBlocked) {
            blockPanelNavigation(false);
        }
        downX = 0;
        downY = 0;
        activeView = null;
        foregroundView = null;
        backgroundLeft = null;
        backgroundRight = null;
        leftLabel = null;
        rightLabel = null;
        rightIcon = null;
        activeAction = null;
        activePosition = RecyclerView.NO_POSITION;
        swiping = false;
    }

    private void blockPanelNavigation(boolean block) {
        panelNavigationBlocked = block;
        View child = recyclerView;
        while (child != null) {
            ViewParent parent = child.getParent();
            if (!(parent instanceof View)) {
                break;
            }
            View parentView = (View) parent;
            if (parentView instanceof ViewGroup) {
                ((ViewGroup) parentView).requestDisallowInterceptTouchEvent(block);
            }
            if (parentView instanceof ViewPager2) {
                break;
            }
            child = parentView;
        }
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
